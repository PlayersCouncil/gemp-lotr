package com.gempukku.lotro.cards.unofficial.pc.errata.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_190_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("destroyer", "57_190");
					put("destroyer2", "57_190");
					put("destroyer3", "57_190");
					put("lemenya", "1_232");    // Ulaire Lemenya: [ringwraith] Nazgul; text only touches [ringwraith] conditions
					put("attea", "103_61");     // Ulaire Attea: a [raider] Nazgul; text needs 3 burdens
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8
					put("silinde", "1_60");     // Silinde: Elf ally, not a companion
					put("sapling", "9_35");     // Sapling of the White Tree: discard to prevent a wound to a [gondor] Man
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulDestroyerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Destroyer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: When you play this minion, you may spot a Nazgûl to add a threat. The Free Peoples player
		 * may make you wound a companion to prevent this.
		 * Skirmish: Remove a threat to make this minion strength +3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("destroyer");

		assertEquals("Morgul Destroyer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedWithANazgulInPlayItMayAddAThreatWhichTheFreePeoplesPlayerMayLetStand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var destroyer = scn.GetShadowCard("destroyer");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(destroyer);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowPlayAvailable(destroyer));
		scn.ShadowPlayCard(destroyer);

		// 3 to play + 2 roaming: a site 4 minion played at site 2.
		assertEquals(twilight - 5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, destroyer);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(destroyer));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDeclineToAddTheThreatAndNobodyIsAskedAnything() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var destroyer = scn.GetShadowCard("destroyer");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(destroyer);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(destroyer);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(destroyer));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetThreats());
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherMinionPlayedDoesNotSetItOffAndASecondCopyAddsOneThreatForItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var destroyer = scn.GetShadowCard("destroyer");
		var destroyer2 = scn.GetShadowCard("destroyer2");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(destroyer2, lemenya);

		scn.StartGame();
		scn.MoveMinionsToTable(destroyer);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// A Nazgul is played with a Morgul Destroyer in play: that Destroyer was not the minion played.
		scn.ShadowPlayCard(lemenya);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetThreats());

		scn.ShadowPlayCard(destroyer2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(destroyer2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(destroyer));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsChooseNo();

		// One threat, for the copy played; the copy already in play adds nothing.
		assertEquals(1, scn.GetThreats());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ANazgulOfAnyCultureIsSpottedButARingwraithOrcIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var destroyer = scn.GetShadowCard("destroyer");
		var destroyer2 = scn.GetShadowCard("destroyer2");
		var destroyer3 = scn.GetShadowCard("destroyer3");
		var attea = scn.GetShadowCard("attea");
		scn.MoveCardsToHand(destroyer2, destroyer3);

		scn.StartGame();
		// A [ringwraith] minion who is an Orc and no Nazgul.
		scn.MoveMinionsToTable(destroyer);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(destroyer2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A Nazgul who is not [ringwraith].
		scn.MoveMinionsToTable(attea);
		scn.ShadowPlayCard(destroyer3);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(destroyer3));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsChooseNo();

		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtTheThreatLimitTheTriggerIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var destroyer = scn.GetShadowCard("destroyer");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(destroyer);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		// Two companions: two threats is the limit.
		scn.AddThreats(2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(2, scn.GetThreatLimit());
		assertEquals(2, scn.GetThreats());
		scn.ShadowPlayCard(destroyer);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayHaveTheShadowPlayerWoundACompanionOfHisChoiceToPreventTheThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.AddWoundsToChar(aragorn, 1);

		var destroyer = scn.GetShadowCard("destroyer");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(destroyer);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(destroyer);
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		// "Make you wound": the Shadow player wounds, and so chooses which companion.
		// The Ring-bearer and a wounded companion; not an ally, not a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(silinde, lemenya, destroyer));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.ShadowChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenTheWoundThatWouldPreventItIsItselfPreventedTheThreatIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sapling = scn.GetFreepsCard("sapling");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(sapling);

		var destroyer = scn.GetShadowCard("destroyer");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(destroyer);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(destroyer);
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsChooseYes();
		scn.ShadowChooseCard(aragorn);

		// Aragorn is a [gondor] Man about to take the wound: the Sapling prevents it.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sapling));
		scn.FreepsAcceptOptionalTrigger();

		assertInDiscard(sapling);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// The prevention was itself prevented, so the threat is added (CR 4.2 "preventing effects").
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishActionRemovesAThreatToMakeThisMinionStrengthPlus3UntilItsSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var destroyer = scn.GetShadowCard("destroyer");
		var destroyer2 = scn.GetShadowCard("destroyer2");

		scn.StartGame();
		scn.MoveMinionsToTable(destroyer, destroyer2);
		scn.AddThreats(2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, destroyer}, new PhysicalCardImpl[]{frodo, destroyer2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(6, scn.GetStrength(destroyer));
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(destroyer));
		scn.ShadowUseCardAction(destroyer);
		assertEquals(9, scn.GetStrength(destroyer));
		assertEquals(1, scn.GetThreats());
		// The other copy, in the other skirmish, is untouched.
		assertEquals(6, scn.GetStrength(destroyer2));

		scn.FreepsPass();
		scn.ShadowUseCardAction(destroyer);
		assertEquals(12, scn.GetStrength(destroyer));
		assertEquals(0, scn.GetThreats());

		// No threat left to remove.
		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(destroyer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(12, scn.GetStrength(destroyer));
		scn.ShadowPass();

		// 12 against Aragorn's 8: Aragorn loses and takes a wound.
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The next skirmish is another skirmish phase: the bonus has gone.
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(6, scn.GetStrength(destroyer));
		assertEquals(6, scn.GetStrength(destroyer2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
