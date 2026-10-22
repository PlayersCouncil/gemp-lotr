package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_093_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("called", "8_93");

					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, vitality 3; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer, vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("barliman", "1_70");    // Barliman Butterbur: an ally, vitality 2; a fellowship ability used only on purpose

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc minion; his text answers only a weapon played on him
					put("troll", "8_102");      // Great Hill Troll: [sauron] Troll minion, besieger and fierce; its play trigger is not reached
					put("patrol", "1_177");     // Goblin Patrol Troop: [moria] Orc minion, no text
					put("hatred", "7_310");     // Sauron's Hatred: a [sauron] support condition, abilities used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CalledAwayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Called Away
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Regroup:</b> Discard a [sauron] minion, spot a companion (except the Ring-bearer), and remove X burdens, where X is that companion's vitality, to return that companion to its owner's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("called");

		assertEquals("Called Away", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RegroupAbilityDiscardsASauronMinionAndRemovesBurdensEqualToTheCompanionsVitalityToReturnHimToHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var called = scn.GetShadowCard("called");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		var chieftain = scn.GetShadowCard("chieftain");
		var troll = scn.GetShadowCard("troll");
		var patrol = scn.GetShadowCard("patrol");
		var hatred = scn.GetShadowCard("hatred");
		scn.MoveCompanionsToTable(gimli, sam, aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToSupportArea(called, hatred);

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		// Sam and Frodo each have 3 vitality left, Aragorn 4, Gimli 3.
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.MoveMinionsToTable(chieftain, troll, patrol);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain, troll, patrol);
		assertTrue(scn.ShadowActionAvailable(called));

		scn.ShadowUseCardAction(called);
		// A [sauron] Orc and a [sauron] Troll; not a [moria] Orc, nor a [sauron] card that is no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, troll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(patrol, hatred, called));
		scn.ShadowChooseCard(chieftain);
		assertInDiscard(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, troll, patrol);

		// Gimli and Sam (Ring-bound, but not the Ring-bearer), each of 3 vitality with 3 burdens to remove.
		// Not Frodo, the Ring-bearer; not Aragorn, whose 4 vitality is more burdens than there are; not an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, aragorn, barliman));
		scn.ShadowChooseCard(gimli);

		// Gimli's vitality is 3: all three burdens go.
		assertEquals(0, scn.GetBurdens());
		assertInHand(gimli);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.FREE_CHARACTERS, sam, aragorn, frodo);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void XIsTheVitalityTheCompanionHasLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var called = scn.GetShadowCard("called");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(called);

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		// Aragorn: vitality 4 with 2 wounds, 2 left.
		scn.AddWoundsToChar(aragorn, 2);
		scn.MoveMinionsToTable(troll);
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(called));
		scn.ShadowUseCardAction(called);
		// The Troll is the only [sauron] minion: discarded unasked.
		assertInDiscard(troll);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, aragorn));
		scn.ShadowChooseCard(aragorn);

		// 3 burdens less Aragorn's 2 vitality left.
		assertEquals(1, scn.GetBurdens());
		assertInHand(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithoutASauronMinionToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var called = scn.GetShadowCard("called");
		var gimli = scn.GetFreepsCard("gimli");
		var chieftain = scn.GetShadowCard("chieftain");
		var patrol = scn.GetShadowCard("patrol");
		var hatred = scn.GetShadowCard("hatred");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(called, hatred);

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.MoveMinionsToTable(patrol);
		assertInZone(Zone.SUPPORT, hatred);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// A [moria] Orc and a [sauron] condition only.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(3, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, patrol);
		assertInZone(Zone.SUPPORT, hatred);
		assertFalse(scn.ShadowActionAvailable(called));

		scn.MoveMinionsToTable(chieftain);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(called));
	}

	@Test
	public void NotOfferedWhileNoCompanionButTheRingBearerHasNoMoreVitalityThanThereAreBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var called = scn.GetShadowCard("called");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(called);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		// Frodo, the Ring-bearer, has 2 vitality left; Gimli 3 and Aragorn 4.
		scn.AddWoundsToChar(frodo, 2);
		scn.MoveMinionsToTable(chieftain);
		assertEquals(2, scn.GetWoundsOn(frodo));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(2, scn.GetBurdens());
		assertFalse(scn.ShadowActionAvailable(called));

		// A third burden pays for Gimli.
		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(called));
	}

	@Test
	public void NotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var called = scn.GetShadowCard("called");
		var gimli = scn.GetFreepsCard("gimli");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(called);

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.MoveMinionsToTable(chieftain);

		// Everything can be paid, but it is no regroup phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(3, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertFalse(scn.ShadowActionAvailable(called));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(called));
	}
}
