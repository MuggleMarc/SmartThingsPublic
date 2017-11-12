/**
 *  My Ecobee Rules
 *
 *  Copyright 2017 Marc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "My Ecobee Rules",
    namespace: "MuggleMarc",
    author: "Marc",
    description: "My custom set of Ecobee rules.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    section("Thermostat:") {
        input "therm", "capability.thermostat", required: true, title: "Thermostat"
    }
    section("Button:") {
        input "button", "capability.button", required: true, title: "Cool button"
    }
    section("Cool for:") {
    	input "runTime", "number", required: false, defaultValue: 10, title: "Minutes?"
    }
    section("Send Push Notifications?") {
    	input "sendPush", "bool", required: false, defaultValue: false, 
        	title: "Send Push Notifications?"
    }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(button, "button.pushed", buttonHandler)
    subscribe(location, "routineExecuted", routineChanged)
}

def routineChanged(evt) {
	def routine = evt.displayName
	log.debug "routineChanged() - $routine executed."
       
    if (routine=="Goodbye!") {
        def message = "Everyone is gone. Setting thermostat to Away."
        log.info message
        if (sendPush) {
         	sendPush(message)
        }
        therm.setThermostatProgram("Away")
    } else if (routine=="I'm Back!") {
    	def message = "Someone is home. Setting thermostat to Resume Program."
        log.info message
        if (sendPush) {
         	sendPush(message)
        }
        therm.resumeProgram()
    }
}

def buttonHandler(evt) {
    log.debug "buttonHander() called: $evt"
        
    // Set therm to low
    def temp = therm.currentTemperature
    def newTemp = temp-3
    log.debug "buttonHandler() - Current temperature = $temp"
    therm.setCoolingSetpoint(newTemp)
    if (sendPush) {
    	sendPush("Setting thermostat to ${newTemp} for $runTime minutes.")
    }
    
    // Set timer to resume old thermostat program
    runIn(60*runTime, thermResume)
}

def thermResume(evt) {
	log.debug "thermResume() called: $evt"
    if (sendPush) {
    	sendPush("Setting thermostat to Resume Program.")
    }
    
    therm.resumeProgram()
}
