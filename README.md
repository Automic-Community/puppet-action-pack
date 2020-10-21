## Getting Started:


###### Description
 
Puppet is a declarative, model-based approach tool for IT automation that helps you manage infrastructure throughout its life cycle, from provisioning and configuration to orchestration and reporting.

The Puppet Action Pack enables you to automate the provisioning, patching, configuration, and management of operating system and application components across enterprise data centers and cloud infrastructures.

###### Actions
 
 1.  Add New Node
 2.  Add Classes to Node Group
 3.  Add/Edit Variable
 4.  Add Class Param to Node Group
 5.  Remove class param from node group
 6.  List Node Group Classes
 7.  Read value from node group variable
 8.  Replace node group classes
 9.  List Class Parameter for Node Group/Class pair
 10. List node groups
 11. List nodes
 12. Remove Node From Node Group
 13. Replace Node groups of a Node
 14. Puppet Run agent
 15. Puppet Run using mcollective agent
 16. List Node Groups for a Node
 17. List Nodes for a Node Group
 18. Install Agent Unix
 19. Install Agent Windows
 20. Start Service
 21. Stop Service
 
 ###### Compatibility:

1. Oracle Java 1.7 or later
2. Puppet Lab Puppet Enterprise 4.5 

###### Prerequisite:

1. Automation Engine should be installed.
2. Automic Package Manager should be installed.
3. ITPA Shared Action Pack should be installed. 
4. Maven

###### Steps to install action pack source code:

1. Clone the code to your machine.
2. Go to the package directory.
3. Run the maven command 'mvn clean package' inside the directory containing the pom.xml file.(source/tools/)
4. Run the command apm upload in the directory which contains package.yml (source/):
   Ex. apm upload -force -u <Name>/<Department> -c <Client-id> -H <Host> -pw <Password> -S AUTOMIC -y -ia -ru


###### Package/Action Documentation

Please refer to the link for [package documentation](source/ae/content/DOCUMENTATION/PCK.AUTOMIC_PUPPET_PUPPET.PUB.DOC.xml)

###### Third party licenses:

The third-party library and license document reference.[Third party licenses](source/ae/content/DOCUMENTATION/PCK.AUTOMIC_PUPPET_PUPPET.PUB.LICENSES.xml)

###### Useful References

1. [About Packs and Plug-ins](https://docs.automic.com/documentation/webhelp/english/AA/12.3/DOCU/12.3/Automic%20Automation%20Guides/help.htm#PluginManager/PM_AboutPacksandPlugins.htm?Highlight=Action%20packs)
2. [Working with Packs and Plug-ins](https://docs.automic.com/documentation/webhelp/english/AA/12.3/DOCU/12.3/Automic%20Automation%20Guides/help.htm#PluginManager/PM_WorkingWith.htm#link10)
3. [Actions and Action Packs](https://docs.automic.com/documentation/webhelp/english/AA/12.3/DOCU/12.3/Automic%20Automation%20Guides/help.htm#_Common/ReleaseHighlights/RH_Plugin_PackageManager.htm?Highlight=Action%20packs)
4. [PACKS Compatibility Mode](https://docs.automic.com/documentation/webhelp/english/AA/12.3/DOCU/12.3/Automic%20Automation%20Guides/help.htm#AWA/Variables/UC_CLIENT_SETTINGS/UC_CLIENT_PACKS_COMPATIBILITY_MODE.htm?Highlight=Action%20packs)
5. [Working with actions](https://docs.automic.com/documentation/webhelp/english/AA/12.3/DOCU/12.3/Automic%20Automation%20Guides/help.htm#ActionBuilder/AB_WorkingWith.htm#link4)
6. [Installing and Configuring the Action Builder](https://docs.automic.com/documentation/webhelp/english/AA/12.3/DOCU/12.3/Automic%20Automation%20Guides/help.htm#ActionBuilder/install_configure_plugins_AB.htm?Highlight=Action%20packs)

###### Distribution: 

In the distribution process, we can download the existing or updated action package from the Automation Engine by using the apm build command.
Example: **apm build -y -H AE_HOST -c 106 -u TEST/TEST -pw password -d /directory/ -o zip -v action_pack_name**
			
			
###### Copyright and License: 

Broadcom does not support, maintain or warrant Solutions, Templates, Actions and any other content published on the Community and is subject to Broadcom Community [Terms and Conditions](https://community.broadcom.com/termsandconditions)

 