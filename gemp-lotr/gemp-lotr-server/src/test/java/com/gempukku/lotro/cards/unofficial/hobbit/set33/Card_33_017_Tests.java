package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_017_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vilya", "33_17");
					// Elrond, Elven Lord: [elven] Elf ally, Wise, 8/4; a start-of-turn heal of a wounded Wise ally (none
					// is wounded at the start of the turn) and a regroup ability, not used
					put("elrond", "30_21");
					// Elrond, Lord of Rivendell: a Decipher Elrond who is an ally; a start-of-turn heal of allies whose
					// home is site 3 (none wounded) and a fellowship ability, not used
					put("elrond1", "1_40");
					// Elrond, Elven Lord: the Elrond who is a companion; a start-of-turn exert-to-heal, not reached
					put("elrondcomp", "7_21");
					// Galadriel, Elven Lady: [elven] Elf ally, Wise, no Elrond; a start-of-turn heal of Gandalf and a
					// skirmish ability, not used
					put("galadriel", "32_13");
					// Gandalf, The Grey: [gandalf] Wizard companion; a trigger on [gandalf] events and a fellowship
					// ability, not used
					put("gandalf", "30_27");
					// Ent Avenger: [gandalf] Ent companion, no Gandalf; strength text that wants 3 Ents
					put("ent", "6_27");
					// Balin, Brother of Dwalin and Nori, Of the Blue Mountains: [dwarven] Dwarf companions, 7/3;
					// a fellowship ability and a maneuver ability, not used
					put("balin", "30_2");
					put("nori", "30_15");
					// Stout and Sturdy: [shire] Maneuver or Skirmish event, "Heal a Hobbit"
					put("stout", "1_315");
					put("stout2", "1_315");
					// Lore of Imladris: [dwarven] maneuver event, "Heal a companion (or 2 Dwarf companions if you
					// spot Elrond)"
					put("lore", "30_13");
					// Shoulder to Shoulder: [elven] support-area condition, "Maneuver: Add (1) and exert a Dwarf to
					// heal an Elf, or ...": the heal of an ally
					put("shoulder", "1_59");

					// Fierce in Despair: [raider] regroup event, "Spot 2 [raider] Men to discard a companion (except
					// the Ring-bearer)"
					put("fierce", "7_148");
					// Easterling Axeman: [raider] Man, 6/2, for Fierce in Despair's spot; strength text that wants
					// 2 burdens
					put("axeman", "4_224");
					put("axeman2", "4_224");
					// Orkish Aggressor: [gundabad] Orc, 6/2; an assignment ability, not used
					put("aggressor", "30_39");
					// Dark Bats: [moria] Creature, 4/2; a Shadow ability, not used
					put("bats", "32_30");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void VilyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Vilya
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Vitality: +1
		 * Game Text: Bearer must be Elrond.<br>Gandalf cannot be discarded.<br>Each time a companion heals during the maneuver phase, you may make him strength +2 until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("vilya");

		assertEquals("Vilya", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void VilyaPlaysOnTheElrondWhoIsAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var elrond = scn.GetFreepsCard("elrond");
		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveAlliesToTable(elrond, galadriel);
		scn.MoveCardsToHand(vilya);
		assertInHand(vilya);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(vilya));
		scn.FreepsPlayCard(vilya);

		// Elrond is the one bearer, taken unasked; not Galadriel, another Wise [elven] Elf ally.
		assertAttachedTo(vilya, elrond);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void VilyaPlaysOnADecipherElrondWhoIsAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var elrond1 = scn.GetFreepsCard("elrond1");
		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveAlliesToTable(elrond1, galadriel);
		scn.MoveCardsToHand(vilya);
		assertInHand(vilya);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(vilya));
		scn.FreepsPlayCard(vilya);

		assertAttachedTo(vilya, elrond1);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void VilyaPlaysOnTheElrondWhoIsACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var elrondcomp = scn.GetFreepsCard("elrondcomp");
		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveAlliesToTable(galadriel);

		scn.StartGame();
		// Laid after the start of the turn, so that his own start-of-turn offer is not made.
		scn.MoveCompanionsToTable(elrondcomp);
		scn.MoveCardsToHand(vilya);
		scn.FreepsRefreshDecision();
		assertInHand(vilya);

		assertTrue(scn.FreepsPlayAvailable(vilya));
		scn.FreepsPlayCard(vilya);

		assertAttachedTo(vilya, elrondcomp);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void VilyaCannotBePlayedWithNoElrond() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var galadriel = scn.GetFreepsCard("galadriel");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveAlliesToTable(galadriel);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(vilya);
		assertInHand(vilya);

		scn.StartGame();
		assertFalse(scn.FreepsPlayAvailable(vilya));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void GandalfCannotBeDiscardedWhileVilyaIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var elrond = scn.GetFreepsCard("elrond");
		var gandalf = scn.GetFreepsCard("gandalf");
		var ent = scn.GetFreepsCard("ent");
		var balin = scn.GetFreepsCard("balin");
		var bilbo = scn.GetRingBearer();
		var fierce = scn.GetShadowCard("fierce");
		var axeman = scn.GetShadowCard("axeman");
		var axeman2 = scn.GetShadowCard("axeman2");
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);
		scn.MoveCompanionsToTable(gandalf, ent, balin);

		scn.StartGame();
		assertAttachedTo(vilya, elrond);
		scn.MoveMinionsToTable(axeman, axeman2);
		scn.MoveCardsToHand(fierce);
		assertInHand(fierce);

		// An Axeman on Balin and one on Gandalf (7 against 6): both companions win, and both Men are still in
		// play at the regroup.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, axeman}, new PhysicalCardImpl[]{gandalf, axeman2});
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		scn.SetTwilight(7);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(fierce));
		scn.ShadowPlayCard(fierce);

		// Gandalf is not offered; Ent Avenger, a [gandalf] companion who is no Gandalf, and Balin are.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ent, balin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gandalf, bilbo));
		scn.ShadowChooseCard(ent);
		assertInDiscard(ent);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithoutVilyaGandalfCanBeDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var gandalf = scn.GetFreepsCard("gandalf");
		var ent = scn.GetFreepsCard("ent");
		var balin = scn.GetFreepsCard("balin");
		var fierce = scn.GetShadowCard("fierce");
		var axeman = scn.GetShadowCard("axeman");
		var axeman2 = scn.GetShadowCard("axeman2");
		scn.MoveAlliesToTable(elrond);
		scn.MoveCompanionsToTable(gandalf, ent, balin);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, axeman2);
		scn.MoveCardsToHand(fierce);
		assertInHand(fierce);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, axeman}, new PhysicalCardImpl[]{gandalf, axeman2});
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		scn.SetTwilight(7);
		scn.FreepsPass();
		scn.ShadowPlayCard(fierce);

		assertTrue(scn.ShadowHasCardChoiceAvailable(gandalf, ent, balin));
		scn.ShadowChooseCard(gandalf);
		assertInDiscard(gandalf);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionHealedInTheManeuverPhaseMayBeStrengthPlus2UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var elrond = scn.GetFreepsCard("elrond");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var stout = scn.GetFreepsCard("stout");
		var bilbo = scn.GetRingBearer();
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);
		scn.MoveCompanionsToTable(balin, nori);

		scn.StartGame();
		assertAttachedTo(vilya, elrond);
		scn.MoveCardsToHand(stout);
		scn.AddWoundsToChar(bilbo, 1);
		scn.AddWoundsToChar(balin, 1);
		scn.MoveMinionsToTable(aggressor, bats);
		assertInHand(stout);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetWoundsOn(balin));

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(3, scn.GetStrength(bilbo));
		assertEquals(7, scn.GetStrength(balin));
		scn.FreepsPlayCard(stout);
		// Bilbo is the one wounded Hobbit: healed unasked.
		assertEquals(0, scn.GetWoundsOn(bilbo));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(vilya));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(5, scn.GetStrength(bilbo));
		// Balin, wounded and not healed, is untouched.
		assertEquals(7, scn.GetStrength(balin));
		assertEquals(1, scn.GetWoundsOn(balin));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Two skirmishes; Bilbo is in neither.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, aggressor}, new PhysicalCardImpl[]{nori, bats});
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(nori);
		// In the second skirmish, after one has resolved: still +2.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(bilbo));

		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(3, scn.GetStrength(bilbo));
	}

	@Test
	public void ACompanionHealedInASkirmishPhaseIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var elrond = scn.GetFreepsCard("elrond");
		var balin = scn.GetFreepsCard("balin");
		var stout = scn.GetFreepsCard("stout");
		var bilbo = scn.GetRingBearer();
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		assertAttachedTo(vilya, elrond);
		scn.MoveCardsToHand(stout);
		scn.AddWoundsToChar(bilbo, 1);
		scn.MoveMinionsToTable(aggressor);
		assertInHand(stout);
		assertEquals(1, scn.GetWoundsOn(bilbo));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, aggressor);
		scn.FreepsResolveSkirmish(balin);
		assertEquals(3, scn.GetStrength(bilbo));
		scn.FreepsPlayCard(stout);
		assertEquals(0, scn.GetWoundsOn(bilbo));

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(vilya));
		assertEquals(3, scn.GetStrength(bilbo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAllyHealedInTheManeuverPhaseIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var elrond = scn.GetFreepsCard("elrond");
		var balin = scn.GetFreepsCard("balin");
		var shoulder = scn.GetFreepsCard("shoulder");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(shoulder);

		scn.StartGame();
		assertAttachedTo(vilya, elrond);
		scn.AddWoundsToChar(elrond, 1);
		scn.MoveMinionsToTable(aggressor);
		assertEquals(1, scn.GetWoundsOn(elrond));

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(8, scn.GetStrength(elrond));
		scn.FreepsUseCardAction(shoulder);
		scn.FreepsChooseCard(balin);
		// Balin exerts; Elrond, the one wounded Elf, is healed unasked.
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(elrond));

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(vilya));
		assertEquals(8, scn.GetStrength(elrond));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TwoCompanionsHealedAtOnceAreEachOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var elrond = scn.GetFreepsCard("elrond");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var lore = scn.GetFreepsCard("lore");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);
		scn.MoveCompanionsToTable(balin, nori);

		scn.StartGame();
		assertAttachedTo(vilya, elrond);
		scn.MoveCardsToHand(lore);
		scn.AddWoundsToChar(balin, 1);
		scn.AddWoundsToChar(nori, 1);
		scn.MoveMinionsToTable(aggressor);
		assertInHand(lore);
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(nori));

		scn.SkipToPhase(Phase.MANEUVER);
		// Elrond is spotted: the two wounded Dwarf companions are healed.
		scn.FreepsPlayCard(lore);
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(nori));

		// The two heals make two offers, put together and in no fixed order: the one taken makes its own
		// companion strength +2 and the other not.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(vilya));
		assertEquals(2, scn.FreepsGetActionCount());
		scn.FreepsAcceptOptionalTrigger();
		int balinStrength = scn.GetStrength(balin);
		int noriStrength = scn.GetStrength(nori);
		// 7 + 7 + 2 between them, and 2 apart: one is 9 and the other 7.
		assertEquals(16, balinStrength + noriStrength);
		assertEquals(2, Math.abs(balinStrength - noriStrength));

		// The other heal's offer is still there; declined, its companion is as he was.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(vilya));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(balinStrength, scn.GetStrength(balin));
		assertEquals(noriStrength, scn.GetStrength(nori));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheSameCompanionHealedTwiceIsOfferedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var elrond = scn.GetFreepsCard("elrond");
		var stout = scn.GetFreepsCard("stout");
		var stout2 = scn.GetFreepsCard("stout2");
		var bilbo = scn.GetRingBearer();
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);

		scn.StartGame();
		assertAttachedTo(vilya, elrond);
		scn.MoveCardsToHand(stout, stout2);
		scn.AddWoundsToChar(bilbo, 2);
		scn.MoveMinionsToTable(aggressor);
		assertInHand(stout, stout2);
		assertEquals(2, scn.GetWoundsOn(bilbo));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(stout);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(vilya));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(5, scn.GetStrength(bilbo));

		scn.ShadowPass();
		scn.FreepsPlayCard(stout2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(vilya));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(7, scn.GetStrength(bilbo));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
