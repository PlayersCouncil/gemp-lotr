package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_06_038_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lights", "6_38");
					put("smeagol", "6_45");     // Sméagol, Poor Creature: [gollum] companion, strength 3, vitality 4; a skirmish ability used only on purpose
					put("smeagol2", "103_116"); // Smeagol, Once Like Us: the [shire] Sméagol, strength 3; an assignment ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 and nothing else
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc, strength 5; +4 and fierce only at 2 wounds on the Ring-bearer
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger is not met when cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DontFollowtheLightsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Don't Follow the Lights
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If Sméagol wins a skirmish, add a burden to discard a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lights");

		assertEquals("Don't Follow the Lights", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SmeagolWinningASkirmishLetsAMinionInOrOutOfHisSkirmishBeDiscardedForABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var lights = scn.GetFreepsCard("lights");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(lights);

		var savage = scn.GetShadowCard("savage");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		assertInZone(Zone.HAND, lights);
		scn.MoveMinionsToTable(savage, hound);
		// 3 + 3 against the Savage's 5
		scn.ApplyAdHocModifier(new StrengthModifier(null, smeagol, null, 3));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{smeagol, savage}, new PhysicalCardImpl[]{aragorn, hound});
		scn.FreepsResolveSkirmish(smeagol);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(lights));

		// Sméagol wins, and the Savage lives through its wound.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(lights));
		assertEquals(0, scn.GetBurdens());

		scn.FreepsChooseOptionalTrigger(lights);
		assertEquals(1, scn.GetBurdens());
		// The minion he beat, and a minion of another culture and race waiting for Aragorn's skirmish; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, hound));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(hound);
		assertInZone(Zone.DISCARD, hound);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.DISCARD, lights);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheShireSmeagolWinningAlsoOffersItAndWithNoMinionLeftTheBurdenIsAddedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lights = scn.GetFreepsCard("lights");
		var smeagol2 = scn.GetFreepsCard("smeagol2");
		scn.MoveCompanionsToTable(smeagol2);
		scn.MoveCardsToHand(lights);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInZone(Zone.HAND, lights);
		scn.MoveMinionsToTable(runner);
		// 3 + 3 against the Runner's 5
		scn.ApplyAdHocModifier(new StrengthModifier(null, smeagol2, null, 3));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol2, runner);
		scn.FreepsResolveSkirmish(smeagol2);
		// The Runner, vitality 1, is killed by its wound: no minion is left.
		scn.FreepsPass();
		scn.ShadowPass();
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(lights));
		assertEquals(0, scn.GetBurdens());

		scn.FreepsChooseOptionalTrigger(lights);
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.DISCARD, lights);
		assertInZone(Zone.FREE_CHARACTERS, smeagol2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenACompanionWhoIsNoSmeagolWinsOrWhenSmeagolLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lights = scn.GetFreepsCard("lights");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(lights);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInZone(Zone.HAND, lights);
		scn.MoveMinionsToTable(savage, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{smeagol, runner});

		// 8 against 5: Aragorn, who is no Sméagol, wins.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(lights));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// 3 against 5: Sméagol loses, and lives through his wound.
		scn.FreepsResolveSkirmish(smeagol);
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(lights));
		assertInZone(Zone.HAND, lights);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
