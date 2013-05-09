#Inleiding
Sinds jaar en dag zijn mensen verantwoordelijk voor verschillende dingen: de tuinbouwer controleert of de plantjes in de kas genoeg water hebben, een serverbeheerder controleert of zijn servers nog lopen en de forens houdt het benzine peil van zijn auto goed in de gaten. 
Anno nu hebben we computers (of andere hulpmiddelen) die een aantal van deze dingen overnemen. Niemand kijkt direct in zijn tank, daar in het metertje op het dashboard voor en tuinbouwers hebben inmiddels geautomatiseerde systemen die de bevochtiging van de planten regelen. 
Met Daemon Master (DM) wil ik de gewone thuisgebruiker in staat stellen om zijn leven te monitoren en daar acties aan te koppelen. Of het nu gaat om het bewateren van de plantjes, het onderhouden van een botnet of een home security system, met DM is het mogelijk.
Daemon Master is een framework met modules. Het bestaat uit 3 onderdelen:
##System
Dit is het hart van Daemon Master. Hierin kan bijvoorbeeld een webinterface staan, of een shellscript die het beheer regelt. In ieder geval: datgene dat zorgt voor de koppeling tussen de monitor en de action. In system is een ‘gateway’ die altijd aanwezig moet zijn. Deze statussen van monitors afvangen en ervoor zorgen dat de juiste action getriggerd wordt. Voor nu zal dit een commandline script zijn geschreven in PHP genaamd listener.php. 
##Monitor
Hierin staan monitors. De toepassingen hiervan zijn vrijwel onbeperkt. Ze kunnen in iedere taal geprogrammeerd worden. De enige voorwaarde is dat een monitors 3 statussen hebben: OK, warning en error en kan communiceren met System. Een monitor luistert dus alleen en doet niets. 
##Action
Actions zijn het tegenovergestelde van monitors: ze doen, maar luisteren niet naar events. Ze worden aangeroepen door system met 3 verschillende aanroepen: OK, warning en alert. Een action is niets anders dan een stuk code dat aangeroepen wordt. 
 
#Het project
In de inleiding heb ik vooral het concept uitgelegd. Wat er opgeleverd gaat worden is een implementatie van het bovenstaande model. Schematisch zal dit het volgende zijn:
-  System
--	Webinterface (CodeIgniter, RedBean PHP, HMVC)
---	Android plug-in (module)
-	Monitor
--	Webserver monitor (OK = 200, Alert = Andere code, Error = geen response)
-	Action
--	Android Push notification

 
#Daemon Master Server Web Interface
De DM web interface verzorgt het management van de server. Het regelt het userbeheer, de installatie van Actions en Monitors en stelt je in staat om scenario’s te maken (Actions en Monitors samen). 
De DM web interface maakt gebruik van het framework CodeIgniter, met als aanvulling de HMVC plug-in (MVC, alleen dan modulair) en Red Bean ORM. 
##Models
-	Auth_model
-	Overview_model
-	User_model
-	Scenario_model
-	Listener_model
-	Install_model
##Views
-	Loginform
-	Overview
-	User management
-	Account beheer
-	Scenario management
-	Module overview
-	Module install
##Controllers
-	Auth
-	Overview
-	User
-	Scenario
-	Listener
-	Install
 
#Daemon Master Android app
De Android app werkt samen met de Action. Het bevat de volgende features:
-	Register on the server by using your credentials
-	Log out (A.K.A unregister device)
-	Receive messages in the following flavours: OK, warning and error
-	Manage messages, example: log OK status, vibarate on warning and vibarate and play audio file on error
-	Temp. mute specific messages
-	Mute them all
##Controllers
-	Main
##Models
-	Message model
-	Server model
##Views
-	Overview
-	Detail
-	Log in
