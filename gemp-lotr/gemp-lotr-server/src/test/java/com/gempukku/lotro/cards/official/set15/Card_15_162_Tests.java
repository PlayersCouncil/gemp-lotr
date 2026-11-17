package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_162_Tests
{

	// The Fellowship-block path: sites 1 to 3 are region 1, sites 4 to 6 region 2.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lurtz", "15_162");
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9, vitality 2; Damage +1 is its whole text
					put("hunt", "1_126");       // Hunt Them Down!: [isengard] Maneuver event, cost 3: make an Uruk-hai fierce until the regroup phase

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability never used
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, strength 6, vitality 3; a skirmish ability never used
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard, vitality 4; his text is about his strength
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] Dwarf ally, vitality 2; a maneuver ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LurtzStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Lurtz, Now Perfected
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * 	Each time Lurtz wins a skirmish, the Free People's player must exert X companions, where X is the current region number.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lurtz");

		assertEquals("Lurtz", card.getBlueprint().getTitle());
		assertEquals("Now Perfected", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void InRegionOneHisWinHasTheFreePeoplesPlayerExertOneCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, gimli, gandalf);
		scn.MoveAlliesToTable(thrarin);

		var lurtz = scn.GetShadowCard("lurtz");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz, guard);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lurtz}, new PhysicalCardImpl[]{gimli, guard});

		// The skirmishes are at site 2, in region 1.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(thrarin));

		// The Guard, an Uruk-hai who is not Lurtz, beats Gimli (9 to 6): two wounds exhaust him, and nobody exerts.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Lurtz beats Aragorn (13 to 8): the skirmish damage first, then his trigger.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The Free Peoples player chooses one companion who can exert, the loser among them; not the exhausted
		// Gimli, the ally or a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gandalf, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, thrarin, lurtz, guard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(gandalf);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InRegionTwoHisWinHasTheFreePeoplesPlayerExertTwoCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gandalf);

		var lurtz = scn.GetShadowCard("lurtz");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(lurtz);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lurtz);

		// The skirmish is at site 4, in region 2.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// Two companions, two different ones.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gandalf, aragorn));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(frodo, gandalf);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NobodyExertsWhenLurtzLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gandalf);

		var lurtz = scn.GetShadowCard("lurtz");

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz);
		// Aragorn is made strong enough to beat him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 6));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lurtz);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(lurtz));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));

		// 13 against 14: Lurtz loses, takes a wound and lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(lurtz));
		assertInZone(Zone.SHADOW_CHARACTERS, lurtz);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void MadeFierceAndWinningTwiceHeHasACompanionExertEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gandalf);

		var lurtz = scn.GetShadowCard("lurtz");
		var hunt = scn.GetShadowCard("hunt");
		scn.MoveCardsToHand(hunt);

		scn.StartGame();
		assertInHand(hunt);
		scn.MoveMinionsToTable(lurtz);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertInHand(hunt);

		// Hunt Them Down! makes the one Uruk-hai fierce.
		scn.ShadowPlayCard(hunt);
		assertTrue(scn.HasKeyword(lurtz, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lurtz);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(8, scn.GetStrength(gandalf));

		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));

		// The fierce skirmish: he beats Gandalf (6, +1 for the Hobbit and +1 for the Man) as well, and a second
		// companion exerts.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gandalf, lurtz);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(gandalf));
		scn.FreepsChooseCard(frodo);

		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
