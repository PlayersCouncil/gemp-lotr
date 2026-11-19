package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_184_Tests
{

	// The Towers path with Uruk Camp 4_335 (Battleground, Plains) as site 2, where the fellowship stands on turn 1.
	// Its "each time an Uruk-hai is played, that minion must exert" reaches no test: every minion is laid by the rig.
	protected VirtualTableScenario GetBattlegroundScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("defender", "4_184");
					put("defender2", "4_184");
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9; Damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, strength 13, vitality 3, no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6; a skirmish ability used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The plain Towers path: site 2 is Eastfold 4_331, plains and no battleground; its Shadow action needs 3
	// [isengard] trackers, and none is in play.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("defender", "4_184");
					put("defender2", "4_184");
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9; Damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, strength 13, vitality 3, no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukDefenderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk Defender
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * <b>Skirmish:</b> Exert this minion at a battleground to make another Uruk-hai strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("defender");

		assertEquals("Uruk Defender", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtABattlegroundExertsToMakeAnotherUrukHaiStrongerForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundScenario();

		var defender = scn.GetShadowCard("defender");
		var defender2 = scn.GetShadowCard("defender2");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(defender, defender2, guard, troop);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		// Aragorn faces the first Defender and the Guard; Gimli the Troop; the second Defender is left unassigned.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, defender},
				new PhysicalCardImpl[]{gimli, troop});
		scn.ShadowAssignToMinions(aragorn, guard);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(defender));
		// The text names no skirmish: a Defender who is in none may use it as well.
		assertTrue(scn.ShadowActionAvailable(defender2));
		assertEquals(0, scn.GetWoundsOn(defender));
		assertEquals(0, scn.GetWoundsOn(defender2));
		assertEquals(9, scn.GetStrength(guard));

		scn.ShadowUseCardAction(defender);
		assertEquals(1, scn.GetWoundsOn(defender));
		// "Exert this minion": the other Defender is not exerted.
		assertEquals(0, scn.GetWoundsOn(defender2));
		// "Another Uruk-hai": of two cultures, one in this skirmish and one in none; not the Defender himself, nor
		// an Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, defender2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(defender, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guard);

		assertEquals(11, scn.GetStrength(guard));
		assertEquals(9, scn.GetStrength(defender));
		assertEquals(9, scn.GetStrength(defender2));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 8 against 9 + 11: Aragorn is overwhelmed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The next skirmish is a phase of its own: the +2 is gone.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(9, scn.GetStrength(guard));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		// Vitality 2 with 1 wound: the first Defender is exhausted and cannot pay; the second still can.
		assertTrue(scn.IsExhausted(defender));
		assertFalse(scn.ShadowActionAvailable(defender));
		assertTrue(scn.ShadowActionAvailable(defender2));
	}

	@Test
	public void IsNotOfferedAwayFromABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defender = scn.GetShadowCard("defender");
		var defender2 = scn.GetShadowCard("defender2");
		var guard = scn.GetShadowCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(defender, defender2, guard);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.FreepsAssignToMinions(aragorn, defender);
		scn.ShadowAssignToMinions(aragorn, guard);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The same board on plains: neither Defender is offered, though each could exert.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(defender));
		assertEquals(0, scn.GetWoundsOn(defender2));
		assertFalse(scn.ShadowActionAvailable(defender));
		assertFalse(scn.ShadowActionAvailable(defender2));
	}
}
