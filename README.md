This is an example of how to create an Apache JMeter testplan in the Java SDK. Essentially creating all of the UI components necessary as well so it can be loaded into the JMeter program itself as a custom script.

Although an example, this repo exists to explain how a custom testplan is made, you would likely do this in cases of automation, paramaterization, source control of JMX plans, but most noteworthy would be the creation and use of a custom sampler. i.e. : https://jmeter.apache.org/api/org/apache/jmeter/protocol/java/sampler/JavaSampler.html

Instead of using the default HTTP Sampler (which does not support special or dynamic auth) you can create your own Sampler, with your own Client which then enables the use of special auth such as OAuth, AWS4, etc. using regular Java code with implementation of your choice.

Again because this is your code in the language of choice you have more liberty for design and implementation choices, as well as the luxury to create unit tests for your components, thus eliminating manual testing of code components in JMeter's UI tool.  
