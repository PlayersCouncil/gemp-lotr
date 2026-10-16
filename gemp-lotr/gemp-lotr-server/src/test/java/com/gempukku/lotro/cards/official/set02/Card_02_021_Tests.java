package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_021_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("erland", "2_21");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion; his text is about his own strength
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf; his bonus needs 3 Ents
					put("inspiration", "11_36");// Inspiration: Skirmish event. "Spot a [gandalf] Wizard to reveal your hand and make a minion strength -3 for each companion in your hand ..."

					put("goblinman1", "2_42");  // Goblin Man: [isengard] Orc. "Shadow: Exert this minion to look at the Free Peoples player's hand."; discarded underground
					put("goblinman2", "2_42");
					put("keyward", "12_105");   // Troll's Keyward: [orc] Orc lurker. "Shadow: Exert Troll's Keyward and reveal your hand to add (1) for each [orc] Troll revealed."
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, damage +1 and nothing else
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, damage +1 and nothing else
					put("reach", "1_137");      // Saruman's Reach: Maneuver event. Exert an Uruk-hai to make the opponent exert 2 companions or put on The One Ring
					put("spies", "2_91");       // Southern Spies: "Response: If the Ring-bearer puts on The One Ring, spot a [sauron] minion to make the Free Peoples player discard his or her hand."
				}},
				// Site 2 is Ettenmoors, a plains site: no Goblin Man is discarded there.
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ErlandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Erland, Advisor to Brand
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: To play, spot Gandalf.<br>Shadow players may not look at or reveal cards in your hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("erland");

		assertEquals("Erland", card.getBlueprint().getTitle());
		assertEquals("Advisor to Brand", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void CanBePlayedOnlyOnceGandalfCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erland = scn.GetFreepsCard("erland");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(erland, gandalf);

		scn.StartGame();

		// A [gandalf] companion who is not Gandalf does not let him be played.
		assertInPlay(avenger);
		assertInHand(erland);
		assertFalse(scn.FreepsPlayAvailable(erland));

		scn.FreepsPlayCard(gandalf);
		assertInPlay(gandalf);
		assertTrue(scn.FreepsPlayAvailable(erland));

		scn.FreepsPlayCard(erland);
		assertInZone(Zone.SUPPORT, erland);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShadowPlayerCannotLookAtYourHandWhileErlandIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erland = scn.GetFreepsCard("erland");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var goblinman1 = scn.GetShadowCard("goblinman1");
		var goblinman2 = scn.GetShadowCard("goblinman2");
		// Erland waits in the discard pile until he is cheated into play below.
		scn.MoveCardsToDiscard(erland);
		assertInDiscard(erland);

		scn.StartGame();
		scn.SetFreepsHand(gandalf, avenger);
		scn.MoveMinionsToTable(goblinman1, goblinman2);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(goblinman1));
		assertEquals(0, scn.GetWoundsOn(goblinman2));
		assertInDiscard(erland);

		// Without Erland the Shadow player is shown the whole hand.
		scn.ShadowUseCardAction(goblinman1);
		assertEquals(1, scn.GetWoundsOn(goblinman1));
		assertTrue(scn.ShadowHasCardChoiceAvailable(gandalf, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(avenger, false));
		assertEquals(2, scn.ShadowGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveAlliesToTable(erland);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, erland);

		// With him, the second Goblin Man still exerts and nothing is shown.
		assertTrue(scn.ShadowActionAvailable(goblinman2));
		scn.ShadowUseCardAction(goblinman2);
		assertEquals(1, scn.GetWoundsOn(goblinman2));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayStillRevealHisOwnHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erland = scn.GetFreepsCard("erland");
		var keyward = scn.GetShadowCard("keyward");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveAlliesToTable(erland);

		scn.StartGame();
		scn.SetShadowHand(troop, savage);
		scn.MoveMinionsToTable(keyward);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SUPPORT, erland);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(keyward));
		int twilight = scn.GetTwilight();

		// The bar is on the hand of Erland's owner: a hand its own owner reveals is shown to the Free Peoples player.
		scn.ShadowUseCardAction(keyward);
		assertEquals(1, scn.GetWoundsOn(keyward));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, false));
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		// No [orc] Troll among them, so nothing is added.
		assertEquals(twilight, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayStillRevealHisOwnHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erland = scn.GetFreepsCard("erland");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var inspiration = scn.GetFreepsCard("inspiration");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(erland);

		scn.StartGame();
		scn.SetFreepsHand(inspiration, avenger);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.SUPPORT, erland);
		assertEquals(2, scn.GetFreepsHandCount());
		int strength = scn.GetStrength(troop);

		// The bar is on Shadow players: the Free Peoples player's own reveal of his hand is shown to the Shadow player.
		scn.FreepsPlayCard(inspiration);
		assertTrue(scn.ShadowHasCardChoiceAvailable(avenger, false));
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();
		// One companion in the hand revealed: the only minion is strength -3.
		assertEquals(strength - 3, scn.GetStrength(troop));
		assertInHand(avenger);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SouthernSpiesStillMakesTheFreePeoplesPlayerDiscardHisHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erland = scn.GetFreepsCard("erland");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var savage = scn.GetShadowCard("savage");
		var sentry = scn.GetShadowCard("sentry");
		var reach = scn.GetShadowCard("reach");
		var spies = scn.GetShadowCard("spies");
		// The Ring-bearer is the only companion, so Saruman's Reach puts the Ring on him unasked.
		scn.MoveAlliesToTable(erland);

		scn.StartGame();
		scn.SetFreepsHand(gandalf, avenger);
		scn.SetShadowHand(reach, spies);
		scn.MoveMinionsToTable(savage, sentry);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertInZone(Zone.SUPPORT, erland);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.ShadowPlayCard(reach);
		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));

		// Erland bars a look or a reveal, and the discard of a whole hand is neither.
		assertTrue(scn.ShadowPlayAvailable(spies));
		scn.ShadowPlayCard(spies);
		assertEquals(0, scn.GetFreepsHandCount());
		assertInDiscard(gandalf, avenger);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
