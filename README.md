
Dieses Projekt ist als Bachelorarbeit an der Johannes-Gutenberg-Universität Mainz entstanden. Es handelt sich nicht um eine offizielle Veröffentlichung der Universität. Für Vollständigkeit und Richtigkeit wird keine Gewähr übernommen.

# Optimierte Erstellung von Trainingsplänen für den Radsport
Mithilfe dieses Java-Programms soll es Radsportlern möglich sein einen optimierten Trainingsplan zu erstellen. Der Trainingsplan wird optimiert nach Benutzereingaben wie Wettkampfsdatum, Wettkampfdisziplin, Dauer, Anzahl der wöchentlichen Trainingstagen und Trainingsstunden. Außerdem werden trainingswissenschaftliche Prinzipien wie Periodisierung, Zyklisierung, Progression, Regeneration und Superkompensation beachtet. Der berechnete Trainingsplan ist über die grafische Oberfläche oder als PDF verfügbar.

Das gesamte Projekt ist Textteil der Arbeit ausfühlich beschrieben. (siehe <a href="./thesis_text.pdf">thesis_text.pdf</a>)
Im Anschluss an die Arbeit wurde sie unter <a href="https://www.butze.digital/trainingplan"> butze.digital</a> auch online zur Verfügung gestellt.


## Modellierung 
Im mathematischen Modell werden die Anforderungen aus der Trainingswissenschaft in das Format der Constraint Programmierung übersetzt. Die Modellierung optimiert jeden Monat gesondert nach Anteilen der Belastungbereiche und berechnet deshalb die Trainingseinheiten für einen Zeitraum von 28 Tagen. Zur Auswahl stehen dabei Einheiten der verschiedenen Trainingsmethoden, um wochenweise die Trainings- umfänge zu füllen. Dauer, Methode und Belastungsbereiche charakterisieren eine Einheit. Später werden die Mesozyklen zu einem Makrozyklus verbunden, der den gesamten Trainingsplan widerspiegelt.
<img src="/text/modellierung.png" alt="UML-Diagram"/>

## Implementierung

Bei der Implementierung der Modellierung wird die Programmiersprache Java verwendet. Nativ bietet sie keine Constraint Programmierung an, aber diesbezüglich wird auf die Open-Source Bibliothek Choco Solver zurückgegriffen.
Aus der Hierarchie der Zyklen, (siehe Modellierung) lassen sich die Objektklassen entwerfen. Auch wenn die übergeordnete Instanz der Makrozyklus ist, erfolgt die Optimierung erst auf der Ebene des Mesozyklus. Dadurch wird jeder Monat unabhängig der Anderen modelliert und der Einsatz des Choco Solvers auf die Meso-Klasse beschränkt. Gesteuert wird die Gewichtung der Belastungsbereiche in den einzelnen Monaten durch zwei Faktoren: Die Länge des Plans (3, 4 oder 5 Monate) bestimmt die Anzahl der periodisierten Meso-Instanzen im Makrozyklus. Des Weiteren steigt bei Mesozyklen die Gewichtung der wettkampfspezifischen Ausdauer bei Näherung an den Wettkampftermin. Diese Vorgaben werden als Minutenziele bereits im Makrozyklus berechnet und dann an die MesoInstanzen weitergegeben. Zusätzlich ist die Modellierung in ein Programm eingebettet, das bereits die Ein- und Ausgabe handhabt. Die Interaktion mit dem Programm ist damit unabhängig von der Trainingsplanerstellung.
Die Anwendung verwendet constraint programming. Dazu wird die Java Bibliothek choco-solver genutzt. Dabei wird jeder Monat gekapselt betrachtet. 

<img src="/text/uml.png" alt="UML-Diagram"/>

Mit JUnit wird die Genauigkeit der Trainingspläne getestet.

## Ergebnis
Die grafische Oberfläche zeigt den gefertigten Trainingsplan an und ermöglicht es diesen als PDF zu exportieren.
<img src="/text/gui.png" alt="grafische Oberfläche"/>
