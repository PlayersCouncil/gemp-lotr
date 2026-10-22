package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_10_099_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shagrat", "10_99");

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, vitality 2; its text answers a weapon played on it
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: Hobbit ally, vitality 3; a fellowship ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShagratStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Shagrat, Captain of Cirith Ungol
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Damage +1</b>.<br>Each time Shagrat loses a skirmish, you may exert a character.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shagrat");

		assertEquals("Shagrat", card.getBlueprint().getTitle());
		assertEquals("Captain of Cirith Ungol", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenShagratLosesASkirmishTheShadowPlayerMayExertACharacterOfAnyKind() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bilbo);

		var shagrat = scn.GetShadowCard("shagrat");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(shagrat, chieftain);
		// Vitality 4 with 3 wounds: Frodo is exhausted.  Aragorn at 8 + 6 = 14 beats Shagrat's 13 and does not
		// overwhelm him.
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 6));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, shagrat);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(shagrat));

		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(shagrat));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shagrat));
		scn.ShadowChooseOptionalTrigger(shagrat);

		// A companion, an ally, another minion and Shagrat himself; not the exhausted Ring-bearer.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, bilbo, chieftain, shagrat));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));
		scn.ShadowChooseCard(bilbo);

		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(1, scn.GetWoundsOn(shagrat));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachLossIsOfferedAndADeclineExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var shagrat = scn.GetShadowCard("shagrat");

		scn.StartGame();
		scn.MoveMinionsToTable(shagrat);
		// Aragorn at 8 + 6 = 14 beats Shagrat's 13 each time; Shagrat, vitality 3, lives through two wounds.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 6));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, shagrat);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(shagrat));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(shagrat));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shagrat));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(shagrat));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// A second move, and a second skirmish against Shagrat in the same turn.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, shagrat);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(shagrat));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shagrat));
		scn.ShadowChooseOptionalTrigger(shagrat);
		scn.ShadowChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(shagrat));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenShagratWinsASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bilbo);

		var shagrat = scn.GetShadowCard("shagrat");

		scn.StartGame();
		scn.MoveMinionsToTable(shagrat);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, shagrat);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bilbo));

		// 13 against 8: Shagrat wins without overwhelming, and his damage +1 makes it two wounds.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(shagrat));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenEveryCharacterIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var shagrat = scn.GetShadowCard("shagrat");

		scn.StartGame();
		scn.MoveMinionsToTable(shagrat);
		// Frodo and Aragorn, vitality 4, with 3 wounds each; Shagrat, vitality 3, with 1, and exhausted by the wound
		// for losing.  Aragorn at 8 + 6 = 14 still beats Shagrat's 13.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(shagrat, 1);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(shagrat));
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 6));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, shagrat);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(shagrat));

		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(shagrat));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
