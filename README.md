# TEDAM-Agent
<a href="http://www.logo.com.tr"><img src="https://www.logo.com.tr/img/logo.png"/></a>

[![Build Status](https://travis-ci.com/logobs/tedam-agent.svg?branch=master)](https://travis-ci.com/logobs/tedam-agent)
[![sonar-quality-gate][sonar-quality-gate]][sonar-url] [![sonar-bugs][sonar-bugs]][sonar-url] [![sonar-vulnerabilities][sonar-vulnerabilities]][sonar-url] [![sonar-duplicated-lines][sonar-dublicated-lines]][sonar-url]

[sonar-url]: https://sonarcloud.io/dashboard?id=com.lbs.tedam%3ATEDAMAgent
[sonar-quality-gate]: https://sonarcloud.io/api/project_badges/measure?project=com.lbs.tedam%3ATEDAMAgent&metric=alert_status
[sonar-bugs]: https://sonarcloud.io/api/project_badges/measure?project=com.lbs.tedam%3ATEDAMAgent&metric=bugs
[sonar-vulnerabilities]: https://sonarcloud.io/api/project_badges/measure?project=com.lbs.tedam%3ATEDAMAgent&metric=vulnerabilities
[sonar-dublicated-lines]: https://sonarcloud.io/api/project_badges/measure?project=com.lbs.tedam%3ATEDAMAgent&metric=duplicated_lines_density

Before running TedamAgent project; you should define logfilePath first. 

To do this: (for eclipse)
	  
	  Right Click AgentGUIController
	  
	  Click Run As -> Run Configurations
	  
	  Go Arguments tab
	  
	  Write: -DlogFilePath=D:/ to VM arguments field. (You can change D path) and apply

Then Right Click “config” folder -> Build Path -> Use folder as source
