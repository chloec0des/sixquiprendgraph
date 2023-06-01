import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import metier.GestionPartie;
import org.json.JSONObject;

import communication.Flag;
import log.MonLogPartie;
import metier.Carte;

public class Partie extends Thread implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<Carte> listCard;
	private int id;
	// HashMap contient la cl� du joueur ainsi que la liste de ses cartes actuels
	private HashMap<String, List<Carte>> playerCard;
	private HashMap<String, Integer> playerBeef;
	private int nbJoueursMax;
	private boolean isProMode;
	private List<List<Carte>> rows;
	private List<Carte> selectedCardByPlayer = null;
	private String nom;
	private boolean isPlayerReach66 = false;
	private boolean isInGame;
	private int choosenRow;

	private MonLogPartie logPartie;

	public Partie(String nom, int nbJoueurs, boolean isProMode, String userNickname) {
		this.listCard = new ArrayList<Carte>();
		this.nbJoueursMax = nbJoueurs;
		this.isProMode = isProMode;
		this.nom = nom;
		isInGame = false;
		playerCard = new HashMap<String, List<Carte>>();
		playerBeef = new HashMap<String, Integer>();
		playerCard.put(userNickname, new ArrayList<Carte>());
		playerBeef.put(userNickname, 0);

		logPartie = new MonLogPartie(this);
	}

	public void setChoosenRow(int row) {
		choosenRow = row;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public void run() {
		logPartie.add("La partie " + id + " a été créée", Level.INFO);
		try {
			synchronized (this) {
				while (getListUser().size() < nbJoueursMax) {
					System.out.println("En attente de joueur...");
					logPartie.add("En attente de joueur ...", Level.INFO);
					this.wait();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("Nombre de joueurs max non atteint");
			logPartie.add("Nombre de joueurs max non atteint", Level.WARNING);
		}
		JSONObject flag = new JSONObject();
		System.out.println("La partie commence ! Bon jeu");
		logPartie.add("Debut de la partie", Level.INFO);
		startGame();
	}

	private void startGame() {
		isInGame = true;
		int cptRound = 1;
		while (!isPlayerReach66) {
			initializeRound();
			currentBeefAllPlayers();
			System.out.println("Fin de la manche " + cptRound);
			logPartie.add("Fin de la manche " + cptRound, Level.INFO);
			cptRound++;
		}
		// TODO : Revoir l'algo dans le cas ou y'a un egalite
		// TODO: ajouter dans client methode "youlose" "youwin"
		List<String> listWinnerAndLoser = GestionPartie.getWinnerAndLoser(getListUser());
		for (int i = 0; i < listWinnerAndLoser.size(); i++) {
			String nickName = listWinnerAndLoser.get(i);
			if (i == 0) {
				System.out.println("Le gagnant est : " + nickName);
				logPartie.add("Le gagnant est : " + nickName, Level.INFO);
			} else {
				System.out.println("Le perdant est : " + nickName);
				logPartie.add("Le perdant est : " + nickName, Level.INFO);
			}
			// TODO : envoyer les informations au client (gagnant, perdant)
		}
		isInGame = false;
	}

	private void initializeRound() {
		rows = new ArrayList<List<Carte>>();
		for (int i = 0; i < 4; i++) {
			rows.add(new ArrayList<Carte>());
		}
		selectedCardByPlayer = null;
		playerCard.clear();
		playerBeef.clear();
		distributeCard();
		distributeCardToPlayers();
	}

	private void distributeCard() {
		for (Carte carte : Carte.values()) {
			listCard.add(carte);
		}
		Collections.shuffle(listCard);
	}

	private void distributeCardToPlayers() {
		int nbCardsPerPlayer = isProMode ? 12 : 8;
		for (String player : getListUser()) {
			List<Carte> playerCards = new ArrayList<Carte>();
			for (int i = 0; i < nbCardsPerPlayer; i++) {
				playerCards.add(listCard.remove(0));
			}
			playerCard.put(player, playerCards);
		}
	}

	private void currentBeefAllPlayers() {
		for (String player : getListUser()) {
			calculateBeef(player);
		}
	}

	private void calculateBeef(String player) {
		List<Carte> playerCards = playerCard.get(player);
		int beef = 0;
		for (Carte carte : playerCards) {
			beef += carte.getValue();
		}
		playerBeef.put(player, beef);
	}

	public void setSelectedCardByPlayer(List<Carte> selectedCardByPlayer) {
		this.selectedCardByPlayer = selectedCardByPlayer;
	}

	public boolean isPlayerReach66(String player) {
		int beef = playerBeef.get(player);
		return beef >= 66;
	}

	public boolean isCardValid(int row, Carte carte) {
		List<Carte> currentRow = rows.get(row);
		if (currentRow.isEmpty()) {
			return true;
		} else {
			Carte lastCard = currentRow.get(currentRow.size() - 1);
			return carte.getValue() >= lastCard.getValue();
		}
	}

	public boolean isGameFull() {
		return getListUser().size() >= nbJoueursMax;
	}

	public boolean isPlayerTurn(String player) {
		List<String> players = getListUser();
		int currentPlayerIndex = players.indexOf(player);
		int currentRound = (int) Math.ceil(players.size() / 2.0);
		int turnPlayerIndex = (currentRound - 1 + currentPlayerIndex) % players.size();
		String turnPlayer = players.get(turnPlayerIndex);
		return player.equals(turnPlayer);
	}

	public int getChoosenRow() {
		return choosenRow;
	}

	public boolean isInGame() {
		return isInGame;
	}

	public List<Carte> getPlayerCards(String player) {
		return playerCard.get(player);
	}

	public List<List<Carte>> getRows() {
		return rows;
	}

	public List<String> getListUser() {
		return new ArrayList<String>(playerCard.keySet());
	}

	public List<String> getListWinnerAndLoser() {
		return listWinnerAndLoser;
	}

	public boolean isProMode() {
		return isProMode;
	}

	public void setProMode(boolean proMode) {
		isProMode = proMode;
	}
}
