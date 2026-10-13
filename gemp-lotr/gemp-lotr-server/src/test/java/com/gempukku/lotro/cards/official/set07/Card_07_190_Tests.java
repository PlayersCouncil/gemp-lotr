package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_190_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("destroyer", "7_190");
					put("destroyer2", "7_190");
					put("destroyer3", "7_190");
					put("lemenya", "1_232");    // Ulaire Lemenya: [ringwraith] Nazgul; his text touches only bearers of [ringwraith] conditions
					put("attea", "103_61");     // Ulaire Attea, Khamul of the East: a [raider] Nazgul; his text needs 3 burdens
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8
					put("gimli", "1_13");       // Gimli, Son of Gloin: companion, strength 6; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Isildur, Bearer of Heirlooms bears the Ring: a [gondor] Man, whose own text acts only at the start of his skirmishes.
	protected VirtualTableScenario GetIsildurScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("destroyer", "7_190");
					put("lemenya", "1_232");    // Ulaire Lemenya: [ringwraith] Nazgul
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the unbound [gondor] Man the Sapling's play spots
					// Sapling of the White Tree: [gondor] artifact in the support area.  "Response: If a [gondor]
					// Man is about to take a wound, discard this artifact to prevent that."
					put("sapling", "9_35");
				}},
				VirtualTableScenario.FellowshipSites,
				"9_33",
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
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: When you play this minion, you may spot a Nazgûl to add 2 threats. The Free Peoples player may wound the Ring-bearer to prevent this.
		 * Skirmish: Remove a threat to make this minion strength +3.
		*/

		var scn = GetScenario();

		var destroyer = scn.GetFreepsCard("destroyer");

		assertEquals("Morgul Destroyer", destroyer.getBlueprint().getTitle());
		assertNull(destroyer.getBlueprint().getSubtitle());
		assertFalse(destroyer.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, destroyer.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, destroyer.getBlueprint().getCulture());
		assertEquals(CardType.MINION, destroyer.getBlueprint().getCardType());
		assertEquals(Race.ORC, destroyer.getBlueprint().getRace());
		assertEquals(2, destroyer.getBlueprint().getTwilightCost());
		assertEquals(6, destroyer.getBlueprint().getStrength());
		assertEquals(2, destroyer.getBlueprint().getVitality());
		assertEquals(4, destroyer.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedWithANazgulInPlayItMayAddTwoThreatsWhichTheFreePeoplesPlayerMayLetStand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		// Three companions: room for three threats.
		scn.MoveCompanionsToTable(aragorn, gimli);

		var destroyer = scn.GetShadowCard("destroyer");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(destroyer);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(3, scn.GetThreatLimit());
		assertEquals(0, scn.GetThreats());
		scn.ShadowPlayCard(destroyer);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(destroyer));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDeclineToAddTheThreatsAndNobodyIsAskedAnything() throws DecisionResultInvalidException, CardNotFoundException {
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
	public void AnotherMinionPlayedDoesNotSetItOffAndASecondCopyAddsItsThreatsForItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

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

		// Two threats, for the copy played; the copy already in play adds nothing.
		assertEquals(2, scn.GetThreats());
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

		assertEquals(2, scn.GetThreats());
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
	public void TheFreePeoplesPlayerMayWoundTheRingBearerToPreventTheThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var destroyer = scn.GetShadowCard("destroyer");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(destroyer);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(destroyer);
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		// The Ring-bearer takes the wound, nobody else does, and no threat is added.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenTheWoundThatWouldPreventItIsItselfPreventedTheThreatsAreAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetIsildurScenario();

		var isildur = scn.GetRingBearer();
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

		// Isildur, the Ring-bearer, is a [gondor] Man about to take the wound: the Sapling prevents it.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sapling));
		scn.FreepsAcceptOptionalTrigger();

		assertInDiscard(sapling);
		assertEquals(0, scn.GetWoundsOn(isildur));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// The prevention was itself prevented, so the threats are added (CR 4.2 "preventing effects").
		assertEquals(2, scn.GetThreats());
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
		var destroyer3 = scn.GetShadowCard("destroyer3");

		scn.StartGame();
		scn.MoveMinionsToTable(destroyer, destroyer2, destroyer3);
		scn.AddThreats(2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, destroyer}, new PhysicalCardImpl[]{frodo, destroyer3});
		// A second Destroyer in the same skirmish as the first.
		scn.ShadowAssignToMinions(aragorn, destroyer2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(6, scn.GetStrength(destroyer));
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(destroyer));
		scn.ShadowUseCardAction(destroyer);
		assertEquals(9, scn.GetStrength(destroyer));
		assertEquals(1, scn.GetThreats());
		// The other copies, one in the same skirmish and one in the other, are untouched.
		assertEquals(6, scn.GetStrength(destroyer2));
		assertEquals(6, scn.GetStrength(destroyer3));

		scn.FreepsPass();
		scn.ShadowUseCardAction(destroyer);
		assertEquals(12, scn.GetStrength(destroyer));
		assertEquals(0, scn.GetThreats());
		assertEquals(6, scn.GetStrength(destroyer2));

		// No threat left to remove.
		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(destroyer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(12, scn.GetStrength(destroyer));
		scn.ShadowPass();

		// 12 and 6 against Aragorn's 8: more than double his strength, so he is killed.
		assertInZone(Zone.DEAD, aragorn);

		// The next skirmish is another skirmish phase: the bonus has gone.
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(6, scn.GetStrength(destroyer));
		assertEquals(6, scn.GetStrength(destroyer2));
		assertEquals(6, scn.GetStrength(destroyer3));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
