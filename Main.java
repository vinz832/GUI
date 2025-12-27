/**
 * Die Klasse Main fungiert als zentraler Einstiegspunkt der Anwendung.
 * In diesem Modul ist die Main-Methode implementiert, welche den Programmablauf initiiert.
 *
 * @Gruppe 16, Moacir, Owen, Matthieu, Vinzenz, Alexander
 * @21.12.2025
 */
public class Main 
{

    public static void main(String[] args) 
    {

        Fabrik testFabrik = new Fabrik();
              
        System.out.println("Willkommen in der AEKI Fabrik.");        
        

        // Auftrag 1: Erfassung von 2 Standard- und 2 Premium-Einheiten
        testFabrik.bestellungAufgeben(2, 2);
        testFabrik.bestellungenAusgeben();

        // Auftrag 2: Anforderung von 5 Standard-Einheiten ohne Premium-Anteil
        testFabrik.bestellungAufgeben(5, 0);
        testFabrik.bestellungenAusgeben();

        // Auftrag 3: Anforderung von 6 Premium-Einheiten ohne Standard-Anteil
        testFabrik.bestellungAufgeben(0, 6);
        testFabrik.bestellungenAusgeben();

        // Auftrag 4: Testlauf mit unzulaessigen negativen Werten (-5, 6)
        testFabrik.bestellungAufgeben(-5, 6);
        testFabrik.bestellungenAusgeben();

        // Auftrag 5: Testlauf bei massiver Ueberschreitung der Kapazitaet (1.000.000, 6)
        testFabrik.bestellungAufgeben(1000000, 6);
        testFabrik.bestellungenAusgeben();

        // Auftrag 6: Prüfung einer leeren Anforderung (0, 0)
        testFabrik.bestellungAufgeben(0, 0);
        testFabrik.bestellungenAusgeben();
        
        // Auftrag 7: Test mit einer groesseren Menge zur Verifizierung (0, 21)
        // testFabrik.bestellungAufgeben(0, 21);
        // testFabrik.bestellungenAusgeben();
    }

}