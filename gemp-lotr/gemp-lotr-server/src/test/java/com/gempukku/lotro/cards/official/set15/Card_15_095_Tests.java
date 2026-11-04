package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_095_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("recruit", "15_95");
					put("recruit2", "15_95");   // a second copy: a minion, and not the one discarded
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7; text on a weapon PLAYED on him only
					put("corps", "11_73");      // Corps of Harad: [men] Man, strength 10; fierce only at a battleground (none on this path)
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7; his text is about being overwhelmed
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BattlefieldRecruitStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Battlefield Recruit
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Skirmish:</b> Discard this minion to make a minion strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("recruit");

		assertEquals("Battlefield Recruit", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ItIsDiscardedToMakeAMinionStrengthPlus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var recruit = scn.GetShadowCard("recruit");
		var recruit2 = scn.GetShadowCard("recruit2");
		var chieftain = scn.GetShadowCard("chieftain");
		var corps = scn.GetShadowCard("corps");

		scn.StartGame();
		scn.MoveMinionsToTable(recruit, recruit2, chieftain, corps);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain}, new PhysicalCardImpl[]{boromir, corps});
		// The Recruit joins the Chieftain against Aragorn; the other copy is left unassigned.
		scn.ShadowAssignToMinions(aragorn, recruit);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, chieftain));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, recruit));
		assertTrue(scn.IsCharAssignedAgainst(boromir, corps));
		assertFalse(scn.IsCharAssigned(recruit2));

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(10, scn.GetStrength(corps));
		assertEquals(9, scn.GetStrength(recruit2));
		assertInZone(Zone.SHADOW_CHARACTERS, recruit);
		assertTrue(scn.ShadowActionAvailable(recruit));

		scn.ShadowUseCardAction(recruit);
		// This copy is discarded, not the other.
		assertInZone(Zone.DISCARD, recruit);
		assertInZone(Zone.SHADOW_CHARACTERS, recruit2);
		// A minion of any culture and race, in this skirmish, waiting for his own or in none, the other copy
		// among them; not a companion, in this skirmish or another.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, corps, recruit2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, boromir));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(corps);

		// 10 + 2
		assertEquals(12, scn.GetStrength(corps));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(9, scn.GetStrength(recruit2));

		// Still so at the next decision of this skirmish.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(12, scn.GetStrength(corps));

		// Aragorn, 8 against the Chieftain's 7, wins.  The Corps' own skirmish is another phase: the bonus is gone.
		assertEquals(0, scn.GetWoundsOn(chieftain));
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(chieftain));
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(10, scn.GetStrength(corps));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ItIsNotOfferedInTheShadowPlayersAssignmentActionsButIsInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var recruit = scn.GetShadowCard("recruit");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(recruit, chieftain);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		// The phase next to its own in which the Shadow player is asked for actions.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(recruit));

		scn.ShadowPass();
		scn.FreepsAssignToMinions(aragorn, chieftain);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(recruit));
		assertInZone(Zone.SHADOW_CHARACTERS, recruit);
	}

	@Test
	public void WithNoOtherMinionItIsOfferedAndDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var recruit = scn.GetShadowCard("recruit");

		scn.StartGame();
		scn.MoveMinionsToTable(recruit);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, recruit);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, recruit);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowActionAvailable(recruit));
		scn.ShadowUseCardAction(recruit);

		// The Recruit was the only minion: it is discarded and nobody is made stronger (a companion is no minion).
		assertInZone(Zone.DISCARD, recruit);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
