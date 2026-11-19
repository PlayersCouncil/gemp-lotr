package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_048_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "17_48");
					put("warrior", "17_60");    // Sunland Warrior: [men] Man, strength 8; a maneuver ability only
					put("runner", "13_90");     // Easterling Runner: [men] Man, strength 8; a Shadow ability only
					put("ted", "18_75");        // Ted Sandyman: [men] Hobbit minion, strength 3; maneuver and regroup abilities only
					put("poleaxe", "11_95");    // Poleaxe: [men] possession, hand weapon, +2, on a [men] minion; a skirmish ability only
					put("poleaxe2", "11_95");
					put("poleaxe3", "11_95");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("scimitar", "1_180");   // Goblin Scimitar: [moria] possession, +2, on a [moria] Orc; a play trigger only
					// Saruman's Staff: an [isengard] ARTIFACT, +2, played on Saruman; he is fierce and damage +1; its
					// Maneuver ability makes the first sentence of Saruman's game text not apply until the regroup phase.
					put("staff", "4_174");
					put("oath", "17_66");       // Wildman's Oath: [men] possession that plays to the support area, cost 2
					put("companies", "12_58");  // Countless Companies: a [men] condition
					put("hides", "4_19");       // Hides: a [dunland] possession that plays to the support area, cost 1
					put("trident", "12_77");    // War Trident: [men] possession, laid only in the Free Peoples player's discard pile
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Saruman, Coldly Still
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Wizard
		 * Strength: 8
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: Each [men] minion bearing a possession is strength +2.<br><b>Skirmish:</b> Exert Saruman to play a [men] possession from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("saruman");

		assertEquals("Saruman", card.getBlueprint().getTitle());
		assertEquals("Coldly Still", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachMenMinionBearingAPossessionIsStrengthPlus2HimselfIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var warrior = scn.GetShadowCard("warrior");
		var runner = scn.GetShadowCard("runner");
		var ted = scn.GetShadowCard("ted");
		var poleaxe = scn.GetShadowCard("poleaxe");
		var poleaxe2 = scn.GetShadowCard("poleaxe2");
		var poleaxe3 = scn.GetShadowCard("poleaxe3");
		var troop = scn.GetShadowCard("troop");
		var scimitar = scn.GetShadowCard("scimitar");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, warrior, runner, ted, troop);
		scn.AttachCardsTo(saruman, poleaxe);
		scn.AttachCardsTo(warrior, poleaxe2);
		scn.AttachCardsTo(ted, poleaxe3);
		scn.AttachCardsTo(troop, scimitar);
		assertAttachedTo(poleaxe, saruman);
		assertAttachedTo(poleaxe2, warrior);
		assertAttachedTo(poleaxe3, ted);
		assertAttachedTo(scimitar, troop);

		// Saruman (a Wizard), a Man and a Hobbit of [men], each bearing a possession: printed + 2 Poleaxe + 2.
		assertEquals(12, scn.GetStrength(saruman));
		assertEquals(12, scn.GetStrength(warrior));
		assertEquals(7, scn.GetStrength(ted));
		// A [men] Man bearing nothing: 8.
		assertEquals(8, scn.GetStrength(runner));
		// A [moria] Orc bearing a possession: 13 + 2 Scimitar.
		assertEquals(15, scn.GetStrength(troop));
	}

	@Test
	public void BearingOnlyHisStaffHeIsNotStrengthenedForAnArtifactIsNoPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		var warrior = scn.GetShadowCard("warrior");
		var poleaxe = scn.GetShadowCard("poleaxe");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, warrior);
		scn.AttachCardsTo(saruman, staff);
		scn.AttachCardsTo(warrior, poleaxe);
		assertAttachedTo(staff, saruman);
		assertAttachedTo(poleaxe, warrior);

		// 8 + 2 Staff, and no +2 of his own text
		assertEquals(10, scn.GetStrength(saruman));
		// 8 + 2 Poleaxe + 2
		assertEquals(12, scn.GetStrength(warrior));
	}

	@Test
	public void SkirmishAbilityExertsHimToPlayAMenPossessionFromHisDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var warrior = scn.GetShadowCard("warrior");
		var poleaxe = scn.GetShadowCard("poleaxe");
		var oath = scn.GetShadowCard("oath");
		var companies = scn.GetShadowCard("companies");
		var hides = scn.GetShadowCard("hides");
		var trident = scn.GetFreepsCard("trident");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToDiscard(poleaxe, oath, companies, hides, trident);
		assertInDiscard(poleaxe, oath, companies, hides, trident);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, warrior);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, warrior);
		scn.ShadowAssignToMinions(frodo, saruman);
		scn.FreepsResolveSkirmish(frodo);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(8, scn.GetStrength(warrior));
		assertTrue(scn.ShadowActionAvailable(saruman));
		scn.ShadowUseCardAction(saruman);

		assertEquals(1, scn.GetWoundsOn(saruman));
		// [men] possessions of his own, a hand weapon and a support-area one; not a [men] condition, a [dunland]
		// possession, or the other player's [men] possession in his own pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(poleaxe, oath));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(companies, hides, trident));
		scn.ShadowChooseCardBPFromSelection(poleaxe);

		// The Poleaxe's own bearer choice
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, warrior));
		scn.ShadowChooseCard(warrior);

		assertAttachedTo(poleaxe, warrior);
		// The first sentence switches on for him: 8 + 2 Poleaxe + 2
		assertEquals(12, scn.GetStrength(warrior));
		assertInDiscard(oath, companies, hides, trident);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var warrior = scn.GetShadowCard("warrior");
		var poleaxe = scn.GetShadowCard("poleaxe");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToDiscard(poleaxe);
		assertInDiscard(poleaxe);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, warrior);
		// Vitality 4 with 3 wounds
		scn.AddWoundsToChar(saruman, 3);
		assertEquals(3, scn.GetWoundsOn(saruman));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, warrior);
		scn.ShadowAssignToMinions(frodo, saruman);
		scn.FreepsResolveSkirmish(frodo);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertFalse(scn.ShadowActionAvailable(saruman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoMenPossessionInHisDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var warrior = scn.GetShadowCard("warrior");
		var poleaxe = scn.GetShadowCard("poleaxe");
		var companies = scn.GetShadowCard("companies");
		var hides = scn.GetShadowCard("hides");
		var trident = scn.GetFreepsCard("trident");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToDiscard(companies, hides, trident);
		assertInDiscard(companies, hides, trident);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, warrior);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, warrior);
		scn.ShadowAssignToMinions(frodo, saruman);
		scn.FreepsResolveSkirmish(frodo);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// The play is the ability's cost: nothing in his pile it could play.
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertFalse(scn.ShadowActionAvailable(saruman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.MoveCardsToDiscard(poleaxe);
		assertInDiscard(poleaxe);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(saruman));
	}

	@Test
	public void WithHisFirstSentenceMadeNotToApplyBySarumansStaffEachMenMinionBearingAPossessionLosesThePlus2UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		// Ketura's ruling (Q-0374: B): the +2 is this Saruman's first sentence, which Saruman's Staff 4_174 may make
		// not apply ("Shadow can choose to *not* shoot himself in the foot"); the answering card of Q-0236.
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		var warrior = scn.GetShadowCard("warrior");
		var poleaxe = scn.GetShadowCard("poleaxe");
		var poleaxe2 = scn.GetShadowCard("poleaxe2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDiscard(poleaxe2);
		assertInDiscard(poleaxe2);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, warrior);
		scn.AttachCardsTo(saruman, staff);
		scn.AttachCardsTo(warrior, poleaxe);
		assertAttachedTo(staff, saruman);
		assertAttachedTo(poleaxe, warrior);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Until the Staff's ability is used the sentence is in force: 8 + 2 Poleaxe + 2.
		assertEquals(12, scn.GetStrength(warrior));
		assertTrue(scn.ShadowActionAvailable(staff));
		scn.ShadowUseCardAction(staff);

		// 8 + 2 Poleaxe; Saruman 8 + 2 Staff, which is no possession either way.
		assertEquals(10, scn.GetStrength(warrior));
		assertEquals(10, scn.GetStrength(saruman));

		// His second sentence still works in the skirmish, while the first is still switched off.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, saruman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(5);
		scn.FreepsPass();
		assertEquals(10, scn.GetStrength(warrior));
		assertTrue(scn.ShadowActionAvailable(saruman));

		// "Until the regroup phase": the +2 is back.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertInZone(Zone.SHADOW_CHARACTERS, warrior);
		assertEquals(12, scn.GetStrength(warrior));
	}
}
