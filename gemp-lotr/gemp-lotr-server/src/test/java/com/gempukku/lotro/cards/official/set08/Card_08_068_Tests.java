package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_068_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("beyond", "8_68");

					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: [wraith] Nazgûl, strength 10, vitality 3; fierce and damage +1 only at 2 burdens, and there are none
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: [raider] Nazgûl, strength 12, fierce; his text needs 3 burdens
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc, no Nazgûl, strength 5, vitality 2; +4 and fierce only at 2 wounds on the Ring-bearer

					put("dwarfguard", "1_7");   // Dwarf Guard: companion, strength 4, vitality 2; needs a Dwarf to be played, so cheated in
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BeyondAllDarknessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Beyond All Darkness
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert a Nazgûl to make him strength +2 and <b>damage +1</b>. If the character he is skirmishing is killed, the Free Peoples player must place his or her hand beneath his or her draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("beyond");

		assertEquals("Beyond All Darkness", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsTheNazgulChosenForStrengthAndDamageAndTheKillPutsTheFreePeoplesHandBeneathHisDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beyond = scn.GetShadowCard("beyond");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		var hand1 = scn.GetFreepsCard("hound");
		var hand2 = scn.GetFreepsCard("nelya");
		scn.MoveCompanionsToTable(dwarfguard);
		scn.MoveCardsToHand(hand1, hand2);
		assertInHand(hand1, hand2);
		scn.MoveCardsToHand(beyond);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, khamul, hound);

		scn.SkipToAssignments();
		// Khamûl and the Hound are left out of the skirmishes.
		scn.FreepsAssignToMinions(dwarfguard, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(dwarfguard);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(0, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(0, scn.GetKeywordCount(khamul, Keyword.DAMAGE));
		assertTrue(scn.ShadowPlayAvailable(beyond));
		scn.ShadowPlayCard(beyond);

		// A Nazgûl of either culture, in this skirmish or in none; not the [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(nelya);

		assertInDiscard(beyond);
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(12, scn.GetStrength(nelya));
		assertEquals(1, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		// The Nazgûl not chosen gets nothing.
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(0, scn.GetKeywordCount(khamul, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		assertEquals(2, scn.GetFreepsHandCount());
		int deckBefore = scn.GetFreepsDeckCount();
		// Nelya, 12, beats the Dwarf Guard, 4, and kills him.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, dwarfguard);

		// The Free Peoples player places his hand, choosing the order (pitfalls, "Several cards placed beneath a
		// draw deck are asked for one at a time").
		assertTrue(scn.FreepsDecisionAvailable("Choose card to put beneath draw deck"));
		scn.FreepsChooseCard(hand1);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deckBefore + 2, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, hand1, hand2);
		// The card chosen first went beneath the deck first; the other, placed unasked, is the bottom card.
		assertEquals(hand2, scn.GetFreepsBottomOfDeck());
		assertEquals(hand1, scn.GetFromBottomOfFreepsDeck(2));
		assertTrue(scn.FreepsAnyDecisionsAvailable());
	}

	@Test
	public void ACharacterWhoLosesToHimAndLivesKeepsTheHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beyond = scn.GetShadowCard("beyond");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var hand1 = scn.GetFreepsCard("hound");
		var hand2 = scn.GetFreepsCard("nelya");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(hand1, hand2);
		assertInHand(hand1, hand2);
		scn.MoveCardsToHand(beyond);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, nelya);
		scn.FreepsPass();

		scn.ShadowPlayCard(beyond);
		assertEquals(12, scn.GetStrength(nelya));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetFreepsHandCount());

		// Nelya, 12, beats Aragorn, 8, short of overwhelming him: 1 wound and 1 for damage +1, of his 4 vitality.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(hand1, hand2);
		assertTrue(scn.FreepsAnyDecisionsAvailable());
	}

	@Test
	public void ACharacterWhoLivesThroughTheSkirmishAndIsKilledInTheFierceRoundKeepsTheHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beyond = scn.GetShadowCard("beyond");
		var khamul = scn.GetShadowCard("khamul");
		var aragorn = scn.GetFreepsCard("aragorn");
		var hand1 = scn.GetFreepsCard("hound");
		var hand2 = scn.GetFreepsCard("nelya");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(hand1, hand2);
		assertInHand(hand1, hand2);
		scn.MoveCardsToHand(beyond);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		// Vitality 4 with one wound.  The event's 2 wounds leave him 1; one wound more in the fierce round kills him.
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, khamul);
		scn.FreepsPass();

		scn.ShadowPlayCard(beyond);
		assertEquals(14, scn.GetStrength(khamul));
		assertEquals(1, scn.GetKeywordCount(khamul, Keyword.DAMAGE));
		assertEquals(2, scn.GetFreepsHandCount());

		// Khamul, 14, beats Aragorn, 8, short of overwhelming him: 1 wound and 1 for damage +1.
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(2, scn.GetFreepsHandCount());

		// Khamul is fierce: the fierce round, a skirmish of its own, in which the event's phase is over.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, khamul);
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(0, scn.GetKeywordCount(khamul, Keyword.DAMAGE));
		scn.PassFierceSkirmishActions();

		// Khamul, 12, beats Aragorn, 8: 1 wound, and he is killed, but not in the skirmish the event was played in.
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(hand1, hand2);
		assertFalse(scn.FreepsDecisionAvailable("Choose card to put beneath draw deck"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void GivenToANazgulWhoseSkirmishIsStillToComeAKillInThisSkirmishKeepsTheHandAndTheBonusIsGoneInHis() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beyond = scn.GetShadowCard("beyond");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var hand1 = scn.GetFreepsCard("hound");
		var hand2 = scn.GetFreepsCard("nelya");
		scn.MoveCompanionsToTable(dwarfguard, aragorn);
		scn.MoveCardsToHand(hand1, hand2);
		assertInHand(hand1, hand2);
		scn.MoveCardsToHand(beyond);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, khamul);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{dwarfguard, khamul}, new PhysicalCardImpl[]{aragorn, nelya});
		scn.FreepsResolveSkirmish(dwarfguard);
		scn.FreepsPass();

		scn.ShadowPlayCard(beyond);
		// Nelya is assigned to Aragorn, whose skirmish is still to come.
		scn.ShadowChooseCard(nelya);
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(12, scn.GetStrength(nelya));
		assertEquals(1, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		assertEquals(2, scn.GetFreepsHandCount());

		// Khamûl, 12, overwhelms the Dwarf Guard, 4: killed, but not by a character Nelya was skirmishing.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, dwarfguard);
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(hand1, hand2);

		// That skirmish was the phase the bonus lasted for.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, nelya));
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(0, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableWhileTheOnlyNazgulIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beyond = scn.GetShadowCard("beyond");
		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		scn.MoveCompanionsToTable(aragorn, dwarfguard);
		scn.MoveCardsToHand(beyond);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, hound);
		// Vitality 3 with two wounds: exhausted.  The Hound could exert, but is no Nazgûl.
		scn.AddWoundsToChar(nelya, 2);
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(hound));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, nelya}, new PhysicalCardImpl[]{dwarfguard, hound});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.GetTwilight() >= 3);
		assertFalse(scn.ShadowPlayAvailable(beyond));
		assertEquals(2, scn.GetWoundsOn(nelya));
	}
}
