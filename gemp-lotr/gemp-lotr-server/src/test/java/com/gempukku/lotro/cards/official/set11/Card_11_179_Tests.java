package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_179_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brawler", "11_179");
					put("brawler2", "11_179");

					put("gimli", "1_13");        // Gimli, Son of Gloin: Dwarf, resistance 6, strength 6, vitality 3; Skirmish: exert Gimli to make him strength +2
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: Man, resistance 6, vitality 4; Maneuver: exert Aragorn to make him defender +1 until the regroup phase
					put("arwen", "1_30");        // Arwen, Daughter of Elrond: Elf, no archer, resistance 6, strength 6, vitality 3; +3 only against a Nazgul
					put("elvensword", "4_64");   // Elven Sword: strength +1, on an Elf; Skirmish: exert bearer (or discard 2 cards from hand) to make a minion skirmishing bearer strength -1
					put("calaglin", "3_10");     // Calaglin, Elf of Lorien: an ally, resistance 0; Regroup: exert Calaglin to wound a minion
				}},
				// Site 2 is Ettenmoors 1_331: "Skirmish: Exert your companion or minion to make that character strength +2."
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BrawlingUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Brawling Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Response:</b> If a special ability of a companion who has resistance 5 or less is used, exert this minion to cancel its effect.
		*/

		var scn = GetScenario();

		var brawler = scn.GetFreepsCard("brawler");

		assertEquals("Brawling Uruk", brawler.getBlueprint().getTitle());
		assertNull(brawler.getBlueprint().getSubtitle());
		assertFalse(brawler.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, brawler.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, brawler.getBlueprint().getCulture());
		assertEquals(CardType.MINION, brawler.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, brawler.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(brawler, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(brawler, Keyword.DAMAGE));
		assertEquals(3, brawler.getBlueprint().getTwilightCost());
		assertEquals(8, brawler.getBlueprint().getStrength());
		assertEquals(3, brawler.getBlueprint().getVitality());
		assertEquals(5, brawler.getBlueprint().getSiteNumber());
	}

	@Test
	public void ExertsToCancelTheSkirmishAbilityOfACompanionOfResistance5() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brawler = scn.GetShadowCard("brawler");
		var brawler2 = scn.GetShadowCard("brawler2");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		// One burden: every companion is resistance -1, so Gimli has 5.
		scn.AddBurdens(1);
		assertEquals(5, scn.GetResistance(gimli));
		scn.MoveMinionsToTable(brawler, brawler2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, brawler);
		// The second copy fights nobody.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(brawler));
		assertEquals(0, scn.GetWoundsOn(brawler2));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		// Each copy makes its own offer.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(brawler));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(brawler2));
		scn.ShadowChooseOptionalTrigger(brawler);

		// The copy whose response it was exerts, and the other does not.
		assertEquals(1, scn.GetWoundsOn(brawler));
		assertEquals(0, scn.GetWoundsOn(brawler2));
		// Cancelled: Gimli's exertion stays paid and he is not strength +2.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CancelsTheManeuverAbilityOfAnotherCompanionOfLowerResistance() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brawler = scn.GetShadowCard("brawler");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		// Two burdens: Aragorn has resistance 4.
		scn.AddBurdens(2);
		assertEquals(4, scn.GetResistance(aragorn));
		scn.MoveMinionsToTable(brawler);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(brawler));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));

		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(brawler));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(brawler));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedForACompanionOfResistance6AndIsOnceABurdenBringsHimTo5() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brawler = scn.GetShadowCard("brawler");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(brawler);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, brawler);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetBurdens());
		assertEquals(6, scn.GetResistance(gimli));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(0, scn.GetWoundsOn(brawler));

		scn.FreepsUseCardAction(gimli);
		// Resistance 6: nothing to answer, and Gimli is strength +2.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(brawler));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(gimli));
		scn.ShadowPass();

		scn.AddBurdens(1);
		assertEquals(5, scn.GetResistance(gimli));
		scn.FreepsUseCardAction(gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(brawler));
		scn.ShadowAcceptOptionalTrigger();

		// The second +2 is cancelled; the first stands.
		assertEquals(1, scn.GetWoundsOn(brawler));
		assertEquals(8, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ThePossessionsAndTheSitesAbilitiesPaidForByACompanionOfResistance5AreNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brawler = scn.GetShadowCard("brawler");
		var brawler2 = scn.GetShadowCard("brawler2");
		var arwen = scn.GetFreepsCard("arwen");
		var elvensword = scn.GetFreepsCard("elvensword");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(arwen, gimli);
		scn.AttachCardsTo(arwen, elvensword);
		assertAttachedTo(elvensword, arwen);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(5, scn.GetResistance(arwen));
		assertEquals(5, scn.GetResistance(gimli));
		scn.MoveMinionsToTable(brawler, brawler2);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, brawler2}, new PhysicalCardImpl[]{gimli, brawler});
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(7, scn.GetStrength(arwen));
		assertEquals(8, scn.GetStrength(brawler2));

		// The Elven Sword's ability, paid for by Arwen's exertion: a possession's ability, not the companion's.
		scn.FreepsUseCardAction(elvensword);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(7, scn.GetStrength(brawler2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();

		// Ettenmoors' ability, paid for by Arwen's exertion: a site's ability.
		var ettenmoors = scn.GetCurrentSite();
		scn.FreepsUseCardAction(ettenmoors);
		scn.FreepsChooseCard(arwen);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(9, scn.GetStrength(arwen));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(brawler));
		assertEquals(0, scn.GetWoundsOn(brawler2));

		// 9 against 7: Arwen wins.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);

		// The control: Gimli's own ability, on the same board, is answered.
		scn.FreepsUseCardAction(gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(brawler));
		scn.ShadowChooseOptionalTrigger(brawler);
		assertEquals(1, scn.GetWoundsOn(brawler));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAllysAbilityIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brawler = scn.GetShadowCard("brawler");
		var calaglin = scn.GetFreepsCard("calaglin");
		scn.MoveAlliesToTable(calaglin);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(brawler);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetResistance(calaglin));
		assertEquals(0, scn.GetWoundsOn(calaglin));
		assertEquals(0, scn.GetWoundsOn(brawler));

		// An ally has resistance 0, but is no companion.
		scn.FreepsUseCardAction(calaglin);
		assertEquals(1, scn.GetWoundsOn(calaglin));
		assertEquals(1, scn.GetWoundsOn(brawler));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brawler = scn.GetShadowCard("brawler");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(5, scn.GetResistance(gimli));
		scn.MoveMinionsToTable(brawler);
		// Vitality 3 with 2 wounds: it cannot exert.
		scn.AddWoundsToChar(brawler, 2);
		assertEquals(2, scn.GetWoundsOn(brawler));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, brawler);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(6, scn.GetStrength(gimli));
		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(2, scn.GetWoundsOn(brawler));
	}
}
