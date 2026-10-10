package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_014_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "101_14");

					// [elven] allies.  Orophin's and Rumil's abilities are archery actions and Celeborn's is a
					// fellowship action that is never used here.
					put("orophin", "1_56");     // Orophin, Lorien Bowman
					put("rumil", "1_57");       // Rumil, Elven Protector
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: cost 2, nothing needed to play him
					put("galadriel", "1_45");   // Galadriel, Lady of Light: ally, cost 3
					put("lorien", "1_53");      // Lorien Elf: [elven] companion, strength 4, no text once in play
					// Albert Dreary, Entertainer from Bree: a [gandalf] ally.  His ability is a maneuver action.
					put("albert", "1_69");
					// Elrond, Elven Lord: a companion, cost 4, played by spotting an Elf.  His own trigger is at
					// the start of a turn, and he is only ever played after that.
					put("elrond", "7_21");
					// Saruman, Keeper of Isengard: [isengard] minion, cost 4, site 4.  With no Uruk-hai about, his
					// text does nothing.
					put("saruman", "3_68");
					// The three Elves again, each as the other kind of character, and never beside a namesake.
					// Celeborn, Lord of the Galadhrim: a companion, cost 2; his trigger is at the start of his skirmishes.
					put("celeborncompanion", "10_7");
					// Elrond, Lord of Rivendell: an ally, cost 4, played by spotting Gandalf; he heals allies of
					// site 3 at the start of a turn, and his ability is a fellowship action never used here.
					put("elrondally", "1_40");
					// Galadriel, Bearer of Wisdom: a companion, cost 3; no text unless she bears an artifact or the Ring.
					put("galadrielcompanion", "9_14");
					// Saruman, Servant of Sauron: [isengard] Shadow follower, unique, cost 1.  A follower is not a
					// character (CR 4.2 "follower", "character"), and its own text does nothing in the support area.
					put("servant", "17_38");
					// The Mirror of Galadriel: a possession in the support area that has "Galadriel" in its
					// title.  It strengthens Elf allies whose home is site 6, and its ability is a maneuver action.
					put("mirror", "1_55");

					put("sleep", "1_84");       // Sleep, Caradhras: [gandalf] event
					put("fire", "1_83");        // Servant of the Secret Fire: [gandalf] event
					put("defiance", "1_37");    // Defiance: [elven] event
					put("axe", "1_3");          // Axe Strike: [dwarven] event
					put("boats", "1_46");       // Gift of Boats: [elven] condition
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Gandalf, Olorin
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: At the start of your fellowship phase, you may spot 3 [elven] allies and exert Gandalf to
		 * take a [Gandalf] or [elven] event from your discard pile into your hand.
		 * Gandalf is strength +1 for each of these characters you can spot: Celeborn, Elrond, Galadriel, Saruman.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("Olorin", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void PlaysFromHandAsACompanionForFourTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(gandalf);

		scn.StartGame();

		assertInHand(gandalf);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(gandalf));

		scn.FreepsPlayCard(gandalf);
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheFellowshipPhaseExertsGandalfToTakeAGandalfOrElvenEventFromDiscardIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var sleep = scn.GetFreepsCard("sleep");
		var defiance = scn.GetFreepsCard("defiance");
		var albert = scn.GetFreepsCard("albert");
		var boats = scn.GetFreepsCard("boats");
		var axe = scn.GetFreepsCard("axe");
		// The opponent's copy of a [gandalf] event, in the opponent's discard pile.  Gandalf's owner has the
		// same card too, but in the draw deck.
		var theirFire = scn.GetShadowCard("fire");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable("orophin", "rumil", "celeborn");
		// A [gandalf] event and an [elven] event; a [gandalf] ally and an [elven] condition, which are not
		// events; and an event of another culture.
		scn.MoveCardsToDiscard(sleep, defiance, albert, boats, axe);
		scn.MoveCardsToDiscard(theirFire);

		scn.StartGame();

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetShadowDiscardCount());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gandalf));

		assertTrue(scn.FreepsHasCardChoiceAvailable(sleep, defiance));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(albert, boats, axe, theirFire));
		assertEquals(2, scn.FreepsGetCardChoiceCount());

		scn.FreepsChooseCardBPFromSelection(sleep);
		assertInZone(Zone.HAND, sleep);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDiscardCount());
		assertInZone(Zone.DISCARD, defiance, albert, boats, axe, theirFire);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsOfferedAgainAtTheStartOfTheNextFellowshipPhaseOfGandalfsOwner() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var sleep = scn.GetFreepsCard("sleep");
		var defiance = scn.GetFreepsCard("defiance");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable("orophin", "rumil", "celeborn");
		scn.MoveCardsToDiscard(sleep, defiance);

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(sleep, defiance));
		scn.FreepsChooseCardBPFromSelection(defiance);
		assertInZone(Zone.HAND, defiance);
		assertInZone(Zone.DISCARD, sleep);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The opponent's fellowship phase comes and goes with nothing asked of Gandalf's owner.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.DISCARD, sleep);

		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));

		// Sleep, Caradhras is now the only event in the discard pile that qualifies, so it is taken without
		// a choice being put.
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.HAND, sleep, defiance);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningLeavesGandalfUnexertedAndTheDiscardPileAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var sleep = scn.GetFreepsCard("sleep");
		var defiance = scn.GetFreepsCard("defiance");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable("orophin", "rumil", "celeborn");
		scn.MoveCardsToDiscard(sleep, defiance);

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDiscardCount());

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertInZone(Zone.DISCARD, sleep, defiance);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWithTwoElvenAlliesAndIsOnceAThirdIsThereAtTheStartOfAFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var lorien = scn.GetFreepsCard("lorien");
		var celeborn = scn.GetFreepsCard("celeborn");
		var sleep = scn.GetFreepsCard("sleep");
		var defiance = scn.GetFreepsCard("defiance");

		// Two [elven] allies, an [elven] companion, and an ally who is not [elven].
		scn.MoveCompanionsToTable(gandalf, lorien);
		scn.MoveAlliesToTable("orophin", "rumil", "albert");
		scn.MoveCardsToDiscard(sleep, defiance);
		scn.MoveCardsToHand(celeborn);

		scn.StartGame();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A third [elven] ally arrives, but the start of this fellowship phase has gone by.
		scn.FreepsPlayCard(celeborn);
		assertInZone(Zone.SUPPORT, celeborn);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetWoundsOn(gandalf));

		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
	}

	@Test
	public void IsNotOfferedWhileGandalfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var orophin = scn.GetFreepsCard("orophin");
		var rumil = scn.GetFreepsCard("rumil");
		var celeborn = scn.GetFreepsCard("celeborn");
		var sleep = scn.GetFreepsCard("sleep");
		var defiance = scn.GetFreepsCard("defiance");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(orophin, rumil, celeborn);
		scn.MoveCardsToDiscard(sleep, defiance);
		// Vitality 4 with 2 wounds: he can exert once more.
		scn.AddWoundsToChar(gandalf, 2);

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCardBPFromSelection(sleep);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.HAND, sleep);

		// Next turn the three allies and an [elven] event in the discard pile are still there, and Gandalf,
		// with 3 wounds, cannot exert.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.SUPPORT, orophin, rumil, celeborn);
		assertInZone(Zone.DISCARD, defiance);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsStrengthPlusOneForEachOfGaladrielElrondCelebornAndSarumanAsTheyArePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var galadriel = scn.GetFreepsCard("galadriel");
		var elrond = scn.GetFreepsCard("elrond");
		var celeborn = scn.GetFreepsCard("celeborn");
		var saruman = scn.GetShadowCard("saruman");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(galadriel, elrond, celeborn);
		scn.MoveCardsToHand(saruman);

		scn.StartGame();

		assertEquals(7, scn.GetStrength(gandalf));

		// An ally, a companion and another ally.
		scn.FreepsPlayCard(galadriel);
		assertEquals(8, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(elrond);
		assertEquals(9, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(celeborn);
		assertEquals(10, scn.GetStrength(gandalf));

		// 9 twilight from those three, + 3 companions and 2 for site 2 as the fellowship moves: enough for
		// Saruman at 4 + 2 roaming.
		scn.SkipToPhase(Phase.SHADOW);
		assertEquals(10, scn.GetStrength(gandalf));

		// A Shadow minion is a character Gandalf's owner can spot.
		scn.ShadowPlayCard(saruman);
		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		// 7 + 1 for each of the four
		assertEquals(11, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnlyTheFourNamedCharactersInPlayCountAndOnlyGandalfIsStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var lorien = scn.GetFreepsCard("lorien");
		var celeborn = scn.GetFreepsCard("celeborn");
		var galadriel = scn.GetFreepsCard("galadriel");
		var mirror = scn.GetFreepsCard("mirror");

		// None of these counts: an [elven] companion and an [elven] ally who are not among the four, a card
		// named for Galadriel that is not her, and Celeborn in the dead pile, where he cannot be spotted.
		scn.MoveCompanionsToTable(gandalf, lorien);
		scn.MoveAlliesToTable("orophin");
		scn.MoveCardsToSupportArea(mirror);
		scn.MoveCardsToDeadPile(celeborn);

		scn.StartGame();

		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(4, scn.GetStrength(lorien));

		// Galadriel herself is worth 1, beside the Mirror, and to Gandalf alone.
		scn.MoveAlliesToTable(galadriel);
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(4, scn.GetStrength(lorien));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CelebornAndGaladrielAsCompanionsAndElrondAsAnAllyAreEachWorthOneToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		// "These characters" are named by title alone: a Celeborn and a Galadriel who are companions and an
		// Elrond who is an ally count as the ally Celeborn, the ally Galadriel and the companion Elrond do.
		var celeborn = scn.GetFreepsCard("celeborncompanion");
		var elrond = scn.GetFreepsCard("elrondally");
		var galadriel = scn.GetFreepsCard("galadrielcompanion");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(celeborn, elrond, galadriel);

		scn.StartGame();

		assertEquals(7, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(celeborn);
		assertInZone(Zone.FREE_CHARACTERS, celeborn);
		assertEquals(8, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(elrond);
		assertInZone(Zone.SUPPORT, elrond);
		assertEquals(9, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(galadriel);
		assertInZone(Zone.FREE_CHARACTERS, galadriel);
		// 7 + 1 for each of the three
		assertEquals(10, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASarumanWhoIsAFollowerIsNotOneOfTheseCharacters() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		// The opponent's Saruman, Servant of Sauron: a card titled Saruman that is a follower, and so not a
		// character (CR 4.2 "follower": followers "are not characters, although they are often named and
		// depicted with images of people from the story").
		var servant = scn.GetShadowCard("servant");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(servant);

		scn.StartGame();

		assertEquals(7, scn.GetStrength(gandalf));

		// 1 for Frodo + 1 for Gandalf + 2 for site 2 as the fellowship moves: enough for the follower at 1.
		// A follower does not roam.
		scn.SkipToPhase(Phase.SHADOW);
		assertEquals(7, scn.GetStrength(gandalf));
		assertTrue(scn.ShadowPlayAvailable(servant));

		scn.ShadowPlayCard(servant);
		assertInZone(Zone.SUPPORT, servant);
		assertEquals(7, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
