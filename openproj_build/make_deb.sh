#/bin/bash
JAVA_OPTS="-Xmx128m"
cd ../openproj_contrib
ant build-contrib build-script build-exchange build-reports
java $JAVA_OPTS -jar ant-lib/proguard.jar @openproj_contrib.conf
java $JAVA_OPTS -jar ant-lib/proguard.jar @openproj_script.conf
java $JAVA_OPTS -jar ant-lib/proguard.jar @openproj_exchange.conf
java $JAVA_OPTS -jar ant-lib/proguard.jar @openproj_exchange2.conf
java $JAVA_OPTS -jar ant-lib/proguard.jar @openproj_reports.conf
cd ../openproj_build
ant -Dbuild_contrib=false
ant -Dbuild_contrib=false deb
