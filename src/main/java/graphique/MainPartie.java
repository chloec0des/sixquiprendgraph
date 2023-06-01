import metier.Partie;
import communication.User;

public class MainPartie {

	public static void main(String[] args) {

		User user1 = new User(1, "Joueur 1");
		User user2 = new User(2, "Joueur 2");

		Partie game = new Partie("6 qui prend", 2, true);
		game.addPlayer(user1);
		game.addPlayer(user2);

		// Démarrez le jeu ici
		game.start();
	}
}
