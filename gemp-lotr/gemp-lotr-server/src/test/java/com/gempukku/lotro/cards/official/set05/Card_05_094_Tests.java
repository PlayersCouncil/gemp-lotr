package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_094_Tests
{

	// The Towers path: the skirmishes of the first turn are at site 2, Eastfold 4_331, a plains (its Shadow
	// ability needs 3 [isengard] trackers).  Rohirrim Herdsman's home, site 3 of that path, is Barrows of
	// Edoras 4_337, a sanctuary and no plains.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("host", "5_94");
					put("host2", "5_94");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3; valiant and nothing else
					put("guard2", "5_83");
					put("mount", "4_287");      // Rider's Mount: [rohan] mount on a [rohan] Man; minions skirmishing bearer exert
					put("mount2", "4_287");
					put("chain", "4_278");      // Heavy Chain: [rohan] armor on a Man, no mount
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, home 3T, strength 4, vitality 2; a regroup ability used only on purpose
					put("aragorn", "4_109");    // Aragorn, Heir of Elendil: a [gondor] Man, defender +1 and nothing else
					put("horse", "4_283");      // Horse of Rohan: a mount on a Man of any culture; minions skirmishing bearer exert
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("troop2", "1_177");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The King path: the skirmishes of the first turn are at site 2, King's Tent 7_335, which is no plains (its
	// Shadow ability needs the Shadow player to have initiative).
	protected VirtualTableScenario GetKingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("host", "5_94");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3; valiant and nothing else
					put("mount", "4_287");      // Rider's Mount: [rohan] mount on a [rohan] Man; minions skirmishing bearer exert
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThunderingHostStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Thundering Host
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.<br><b>Skirmish:</b> Discard this condition to make a mounted [rohan] Man strength +3 (and heal that Man if at a plains).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("host");

		assertEquals("Thundering Host", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SkirmishAbilityDiscardsThisCopyToMakeAMountedRohanManStrengthPlus3AndHealHimAtAPlainsForThisSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetFreepsCard("host");
		var host2 = scn.GetFreepsCard("host2");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		var chain = scn.GetFreepsCard("chain");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var horse = scn.GetFreepsCard("horse");
		scn.MoveCompanionsToTable(guard, guard2, aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToSupportArea(host, host2);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(herdsman, mount2);
		scn.AttachCardsTo(guard2, chain);
		scn.AttachCardsTo(aragorn, horse);

		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.AddWoundsToChar(guard, 2);
		scn.AddWoundsToChar(guard2, 1);
		scn.AddWoundsToChar(herdsman, 1);
		scn.MoveMinionsToTable(troop, troop2);
		assertInZone(Zone.SUPPORT, host, host2);
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(guard2));
		assertEquals(1, scn.GetWoundsOn(herdsman));

		// The Free Peoples player's assignment actions, the phase before the skirmishes: not offered.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(host));
		assertFalse(scn.FreepsActionAvailable(host2));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, troop},
				new PhysicalCardImpl[]{aragorn, troop2});
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishingAgainst(guard, troop));
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(host));
		assertInZone(Zone.SUPPORT, host, host2);
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(herdsman));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(guard2));
		assertEquals(1, scn.GetWoundsOn(herdsman));

		scn.FreepsUseCardAction(host);
		// Mounted [rohan] Men: the companion in the skirmish and the ally in none.  Not the [rohan] Man whose
		// possession is no mount, nor the mounted Man of another culture.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, herdsman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard2, aragorn));
		scn.FreepsChooseCard(guard);

		// The copy used is discarded; the other stays.
		assertInDiscard(host);
		assertInZone(Zone.SUPPORT, host2);
		// 6 + 3, and at a plains one of his two wounds healed; nobody else changed.
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(4, scn.GetStrength(herdsman));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(guard2));

		// Still +3 at the next decision of the skirmish.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(guard));
		scn.PassSkirmishActions();
		// 9 against 13: he loses and takes a wound.
		assertEquals(2, scn.GetWoundsOn(guard));

		// Gone in the next skirmish.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop2));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AwayFromAPlainsTheManIsStrengthPlus3AndNotHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingScenario();

		var host = scn.GetFreepsCard("host");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(host);
		scn.AttachCardsTo(guard, mount);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddWoundsToChar(guard, 2);
		scn.MoveMinionsToTable(troop);
		assertInZone(Zone.SUPPORT, host);
		assertEquals(2, scn.GetWoundsOn(guard));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, troop);
		scn.FreepsResolveSkirmish(guard);
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(2, scn.GetWoundsOn(guard));

		scn.FreepsUseCardAction(host);
		assertInDiscard(host);
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	@Ignore("F-0518: \"heal that Man if at a plains\" goes by the fellowship's site, but an ally is at his home site (CR 4.2 ally): the Herdsman, home Barrows of Edoras, is healed with the fellowship at Eastfold")
	public void AnAllyChosenWithTheFellowshipAtAPlainsIsAtHisHomeSiteAndIsNotHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetFreepsCard("host");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var mount2 = scn.GetFreepsCard("mount2");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToSupportArea(host);
		scn.AttachCardsTo(herdsman, mount2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddWoundsToChar(herdsman, 1);
		scn.MoveMinionsToTable(troop);
		assertEquals(1, scn.GetWoundsOn(herdsman));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, troop);
		scn.FreepsResolveSkirmish(guard);
		// The fellowship is at a plains; the Herdsman, an ally, is at his home site 3, Barrows of Edoras.
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(scn.GetFreepsSite(3), Keyword.PLAINS));
		assertEquals(4, scn.GetStrength(herdsman));
		assertEquals(1, scn.GetWoundsOn(herdsman));

		// The only mounted [rohan] Man: chosen unasked.
		scn.FreepsUseCardAction(host);
		assertEquals(7, scn.GetStrength(herdsman));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
