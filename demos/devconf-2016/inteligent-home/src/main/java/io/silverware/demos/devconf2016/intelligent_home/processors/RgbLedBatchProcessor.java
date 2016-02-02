/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2010 - 2013 the original author or authors.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -----------------------------------------------------------------------/
 */
package io.silverware.demos.devconf2016.intelligent_home.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import io.silverware.demos.devconf2016.intelligent_home.Configuration;

/**
 * @author <a href="mailto:pavel.macik@gmail.com">Pavel Macík</a>
 */
public class RgbLedBatchProcessor implements Processor {

   private static final Logger log = Logger.getLogger(RgbLedBatchProcessor.class);

   private Configuration config = Configuration.getInstance();

   public RgbLedBatchProcessor(Configuration config) {
      super();
      this.config = config;
   }

   @Override
   public void process(final Exchange exchange) throws Exception {
      final Message msg = exchange.getIn();
      final StringBuffer pwmBatch = new StringBuffer();

      final String[] batchLines = msg.getBody(String.class).split("\n");

      boolean first = true;
      for (String batchLine : batchLines) {
         // input message batch line "<led>;<channel(r,g,b)>;<value(0-100)>"
         //TODO: add batch line format validation
         if (log.isDebugEnabled()) {
            log.debug("Batch line: " + batchLine);
         }
         final String[] parts = batchLine.split(";");
         final int led = Integer.valueOf(parts[0]);
         final String channel = parts[1];
         final int value = Integer.valueOf(parts[2]);
         if (log.isTraceEnabled()) {
            log.trace("LED #: " + led);
            log.trace("LED channel: " + channel);
            log.trace("Value: " + value);
         }
         // output message batch line "<i2c address>;<pwm output(0-15)>;<value(0-4095)>"
         if (first) {
            first = false;
         } else {
            pwmBatch.append("\n");
         }
         pwmBatch.append(config.getPca9685Address(led, channel)); // I2C address
         pwmBatch.append(";");
         pwmBatch.append(config.getRgbLedPwm(led, channel)); // pwm output
         pwmBatch.append(";");
         pwmBatch.append(Integer.valueOf((int) (40.95 * value))); // pwm value
      }
      msg.setBody(pwmBatch.toString());
   }
}
