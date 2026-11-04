package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_026_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("onod", "17_26");

					put("guardian", "5_17");    // Forest Guardian: [gandalf] Ent companion, unhasty; an assignment ability used only on purpose
					put("treebeard", "4_103");  // Treebeard, Earthborn: [gandalf] Ent ally; a response and a fellowship ability about unbound Hobbits
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] companion who is no Ent; +1 for each race he spots
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, here a card in hand
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] Ent companion, here a card in hand
					put("merchant", "7_97");    // Gondorian Merchant: an ally, here a card in hand

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; Damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WoodlandOnodStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Woodland Onod
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Ent
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: <b>Skirmish:</b> Discard a companion from hand to make an Ent strength +2.
		*/

		var scn = GetScenario();

		var onod = scn.GetFreepsCard("onod");

		assertEquals("Woodland Onod", onod.getBlueprint().getTitle());
		assertNull(onod.getBlueprint().getSubtitle());
		assertFalse(onod.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, onod.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, onod.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, onod.getBlueprint().getCardType());
		assertEquals(Race.ENT, onod.getBlueprint().getRace());
		assertEquals(4, onod.getBlueprint().getTwilightCost());
		assertEquals(7, onod.getBlueprint().getStrength());
		assertEquals(4, onod.getBlueprint().getVitality());
		assertEquals(6, onod.getBlueprint().getResistance());
	}

	@Test
	public void SkirmishAbilityDiscardsACompanionFromHandToMakeAnEntStrengthPlusTwoForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var onod = scn.GetFreepsCard("onod");
		var guardian = scn.GetFreepsCard("guardian");
		var treebeard = scn.GetFreepsCard("treebeard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(onod, guardian, gandalf);
		scn.MoveAlliesToTable(treebeard);

		var aragorn = scn.GetFreepsCard("aragorn");
		var avenger = scn.GetFreepsCard("avenger");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCardsToHand(aragorn, avenger, merchant);
		assertInHand(aragorn, avenger, merchant);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{onod, troop}, new PhysicalCardImpl[]{gandalf, savage});

		scn.FreepsResolveSkirmish(onod);
		int onodStrength = scn.GetStrength(onod);
		int treebeardStrength = scn.GetStrength(treebeard);
		int gandalfStrength = scn.GetStrength(gandalf);
		assertEquals(8, scn.GetStrength(guardian));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(onod));

		scn.FreepsUseCardAction(onod);
		// Any companion in hand, of either culture and race; not an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, avenger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertInDiscard(aragorn);
		assertInHand(avenger, merchant);

		// Any Ent: Onod himself, an Ent companion who is in no skirmish and an Ent ally; not a [gandalf] companion
		// who is no Ent.
		assertTrue(scn.FreepsHasCardChoiceAvailable(onod, guardian, treebeard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guardian);

		assertEquals(10, scn.GetStrength(guardian));    // 8 + 2
		assertEquals(onodStrength, scn.GetStrength(onod));
		assertEquals(treebeardStrength, scn.GetStrength(treebeard));
		assertEquals(gandalfStrength, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gone in the next skirmish, a phase of its own.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gandalf);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(guardian));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoCompanionInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var onod = scn.GetFreepsCard("onod");
		scn.MoveCompanionsToTable(onod);

		// An ally in hand, no companion.
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCardsToHand(merchant);
		assertInHand(merchant);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(onod, troop);

		scn.FreepsResolveSkirmish(onod);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(onod));
	}
}
