package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_024_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer; the skirmishes of
	// turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bolg", "31_24");

					put("balin", "30_2");       // Balin, Brother of Dwalin: Dwarf companion, strength 7, vitality 3; no damage bonus
					put("nori", "30_15");       // Nori, Of the Blue Mountains: Dwarf companion, strength 7; a maneuver ability not used
					// Warg: [gundabad] mount, bearer a [gundabad] Orc, who is fierce; a regroup ability not used
					put("warg", "31_27");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BolgStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Bolg, Son of Azog
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gundabad
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Damage +1</b>.
		 * Each time Bolg wins a skirmish, you may exert him twice to add a doubt.
		 */

		var scn = GetScenario();

		var card = scn.GetShadowCard("bolg");

		assertEquals("Bolg", card.getBlueprint().getTitle());
		assertEquals("Son of Azog", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GUNDABAD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningBolgMayExertTwiceToAddADoubt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bolg = scn.GetShadowCard("bolg");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(bolg);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, bolg);
		scn.FreepsResolveSkirmish(balin);

		assertEquals(0, scn.GetWoundsOn(bolg));
		assertEquals(0, scn.GetBurdens());
		scn.PassSkirmishActions();
		// 9 against 7: Bolg wins, and Balin takes 2 wounds (damage +1).
		assertEquals(2, scn.GetWoundsOn(balin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bolg));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(bolg));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningBolgWhoCannotExertTwiceIsOfferedNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bolg = scn.GetShadowCard("bolg");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(bolg);
		// Vitality 3 with 1 wound: Bolg can exert once more, not twice.
		scn.AddWoundsToChar(bolg, 1);
		assertEquals(1, scn.GetWoundsOn(bolg));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, bolg);
		scn.FreepsResolveSkirmish(balin);

		assertEquals(0, scn.GetBurdens());
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(balin));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(bolg));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BolgLosingASkirmishIsOfferedNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bolg = scn.GetShadowCard("bolg");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(bolg);
		// Balin at 12, to beat Bolg's 9 without overwhelming him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, balin, null, 5));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, bolg);
		scn.FreepsResolveSkirmish(balin);

		assertEquals(0, scn.GetWoundsOn(bolg));
		assertEquals(0, scn.GetBurdens());
		scn.PassSkirmishActions();
		// Bolg takes a wound and lives.
		assertEquals(1, scn.GetWoundsOn(bolg));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DeclinedBolgIsOfferedAgainForHisFierceWin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bolg = scn.GetShadowCard("bolg");
		var warg = scn.GetShadowCard("warg");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		scn.MoveCompanionsToTable(balin, nori);

		scn.StartGame();
		scn.MoveMinionsToTable(bolg);
		// The Warg makes Bolg fierce.
		scn.AttachCardsTo(bolg, warg);
		assertAttachedTo(warg, bolg);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, bolg);
		scn.FreepsResolveSkirmish(balin);

		assertEquals(0, scn.GetWoundsOn(bolg));
		assertEquals(0, scn.GetBurdens());
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bolg));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(bolg));
		assertEquals(0, scn.GetBurdens());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(nori, bolg);
		scn.FreepsResolveSkirmish(nori);
		scn.PassFierceSkirmishActions();
		// 9 + 2 against 7: he wins again.
		assertEquals(2, scn.GetWoundsOn(nori));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bolg));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(bolg));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
