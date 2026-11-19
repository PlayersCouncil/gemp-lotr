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

public class Card_17_079_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("invader", "17_79");
					put("invader2", "17_79");
					put("threatening", "17_91"); // Threatening Warg: [orc] mount for an [orc] Orc of strength 10 or less; +2, +1, bearer fierce; its play trigger never set off (cheated on)
					put("beckoning", "12_83");   // The Beckoning Shadow: an [orc] condition, kept in hand
					put("clash", "17_68");       // Chaotic Clash: another [orc] condition, kept in hand
					put("weary", "4_212");       // Weary: an [isengard] condition, kept in hand
					put("gear", "17_90");        // Rider's Gear: an [orc] possession, kept in hand
					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc minion, strength 13, no text

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only used on purpose
					put("gandalf", "1_72");      // Gandalf, Friend of the Shirefolk: strength 6, +1 for a Man and +1 for a Hobbit here: 8
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishInvaderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Orkish Invader
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: At the start of each skirmish involving this minion, you may reveal an [orc] condition from hand to wound the companion this minion is skirmishing twice and then discard this minion.
		*/

		var scn = GetScenario();

		var invader = scn.GetFreepsCard("invader");

		assertEquals("Orkish Invader", invader.getBlueprint().getTitle());
		assertNull(invader.getBlueprint().getSubtitle());
		assertFalse(invader.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, invader.getBlueprint().getSide());
		assertEquals(Culture.ORC, invader.getBlueprint().getCulture());
		assertEquals(CardType.MINION, invader.getBlueprint().getCardType());
		assertEquals(Race.ORC, invader.getBlueprint().getRace());
		assertEquals(4, invader.getBlueprint().getTwilightCost());
		assertEquals(10, invader.getBlueprint().getStrength());
		assertEquals(2, invader.getBlueprint().getVitality());
		assertEquals(4, invader.getBlueprint().getSiteNumber());
	}

	@Test
	public void RevealingAnOrcConditionWoundsTheCompanionItSkirmishesTwiceAndDiscardsTheInvader() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gandalf);

		var invader = scn.GetShadowCard("invader");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");
		var clash = scn.GetShadowCard("clash");
		var weary = scn.GetShadowCard("weary");
		var gear = scn.GetShadowCard("gear");
		scn.MoveCardsToHand(beckoning, clash, weary, gear);

		scn.StartGame();
		assertInZone(Zone.HAND, beckoning, clash, weary, gear);
		scn.MoveMinionsToTable(invader, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, invader}, new PhysicalCardImpl[]{gandalf, troop});
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(invader));
		scn.ShadowAcceptOptionalTrigger();

		// The cost: an [orc] condition, not a condition of another culture nor an [orc] card that is no condition.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(beckoning, clash));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(weary, gear));
		scn.ShadowChooseCard(beckoning);

		// It is shown to the Free Peoples player alone, and stays in hand.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(beckoning, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertInZone(Zone.HAND, beckoning, clash, weary, gear);

		// Two wounds on Aragorn, none on Gandalf in the other skirmish, and the Invader is discarded.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInDiscard(invader);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingSkirmishChoice(scn.P1));
	}

	@Test
	public void AnExhaustedCompanionItSkirmishesIsWoundedAndKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		// Aragorn is exhausted: 3 wounds on 4 vitality.  An exertion could not be placed on him.
		scn.AddWoundsToChar(aragorn, 3);

		var invader = scn.GetShadowCard("invader");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(beckoning);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.HAND, beckoning);
		scn.MoveMinionsToTable(invader);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, invader);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(invader));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DEAD, aragorn);
		assertInDiscard(invader);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.HAND, beckoning);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningRevealsNothingAndTheSkirmishGoesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var invader = scn.GetShadowCard("invader");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(beckoning);

		scn.StartGame();
		assertInZone(Zone.HAND, beckoning);
		scn.MoveMinionsToTable(invader);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, invader);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(invader));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, invader);
		assertInZone(Zone.HAND, beckoning);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoOrcConditionInHandNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var invader = scn.GetShadowCard("invader");
		var weary = scn.GetShadowCard("weary");
		var gear = scn.GetShadowCard("gear");
		scn.MoveCardsToHand(weary, gear);

		scn.StartGame();
		assertInZone(Zone.HAND, weary, gear);
		scn.MoveMinionsToTable(invader);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, invader);
		scn.FreepsResolveSkirmish(aragorn);

		// A condition of another culture and an [orc] possession cannot pay: the skirmish starts with nothing asked.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(invader));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, invader);
	}

	@Test
	public void EachCopyIsOfferedOnlyAtTheStartOfItsOwnSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gandalf);

		var invader = scn.GetShadowCard("invader");
		var invader2 = scn.GetShadowCard("invader2");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(beckoning);

		scn.StartGame();
		assertInZone(Zone.HAND, beckoning);
		scn.MoveMinionsToTable(invader, invader2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, invader}, new PhysicalCardImpl[]{gandalf, invader2});

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(invader));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(invader2));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The Invader, 10, beats Aragorn, 8.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));

		scn.FreepsResolveSkirmish(gandalf);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(invader2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(invader));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();

		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(invader2);
		assertInZone(Zone.SHADOW_CHARACTERS, invader);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AFierceInvaderIsOfferedAgainAtItsFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var invader = scn.GetShadowCard("invader");
		var threatening = scn.GetShadowCard("threatening");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(beckoning);

		scn.StartGame();
		assertInZone(Zone.HAND, beckoning);
		scn.MoveMinionsToTable(invader);
		scn.AttachCardsTo(invader, threatening);
		assertAttachedTo(threatening, invader);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, invader);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(invader));
		scn.ShadowDeclineOptionalTrigger();
		// The Invader, 10 + 2, beats Aragorn, 8.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, invader);

		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, invader);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(invader));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInDiscard(invader, threatening);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
