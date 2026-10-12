package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_065_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shapes1", "102_65");
					put("shapes2", "102_65");

					// Twilight minions.
					// Corpse Lights: a [sauron] Wraith, strength 7, vitality 2, site 4.  A skirmish ability, never used.
					put("lights", "6_99");
					// Marsh Wight: a [sauron] Wraith, strength 9, vitality 3, site 4.  Damage +1 only at 4 burdens.
					put("wight", "102_61");
					// Ulaire Nelya, Ringwraith in Twilight: a [ringwraith] Nazgul and no Wraith, strength 10,
					// vitality 3, site 2.  Fierce and damage +1 only at 2 burdens or 2 wounds on the Ring-bearer.
					put("nelya", "2_84");
					// Ulaire Attea, The Easterling: another [ringwraith] Nazgul, vitality 3.  Its text is for
					// when it is played, and it is never played here.
					put("attea", "2_82");

					// Minions that are not twilight.
					// Undead of Angmar: a Wraith, of the [ringwraith] culture, vitality 3, site 2.  Its texts need
					// a wounded companion and another [ringwraith] Wraith, or two exertions in the Shadow phase.
					put("undead", "16_6");
					// Goblin Patrol Troop: no text.  Vitality 3, site 4.
					put("troop", "1_177");

					// Cover of Darkness, Omen of Despair: a twilight [sauron] condition, and so twilight without
					// being a minion.  Its ability needs a Troll.
					put("cover", "103_93");

					// Dead Man of Dunharrow: a [gondor] Wraith who is a companion.  His text is for when he is played.
					put("deadman", "10_27");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf and an archer
					// Pippin, Wearer of Black and Silver: "Skirmish: If Pippin is not assigned to a skirmish, return
					// him to your hand to wound a roaming minion twice."
					put("pippin", "7_324");
					// Calaglin, Elf of Lorien: an ally.  "Regroup: Exert Calaglin to wound a minion."  He is an
					// archer too, so he is put down after the archery phase.
					put("calaglin", "3_10");
					// Defend It and Hope: "Maneuver: Exert a [gondor] Man to wound a minion once (or twice if
					// that minion is an Uruk-hai)."
					put("defend1", "4_115");
					put("defend2", "4_115");
					// Stand Against Darkness: "Maneuver: Exert an Elf to discard a [sauron] minion, a [sauron]
					// condition, or a [sauron] possession."
					put("stand", "1_63");
					// O Elbereth! Gilthoniel!: borne by the Ring-bearer.  "Skirmish: Discard this condition to take
					// off The One Ring or to cancel a skirmish involving the Ring-bearer and a Nazgul."
					put("elbereth", "2_108");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The Great Ring: "Maneuver: Wear The One Ring until the regroup phase."  It costs nothing to put on.
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void ShapestoSeeNottoTouchStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Shapes to See, Not to Touch
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a Wraith.
		 * 	While you can spot 3 twilight minions, twilight minions may not take wounds except during skirmishes.
		 * 	When the ringbearer takes off the One Ring, discard this condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shapes1");

		assertEquals("Shapes to See, Not to Touch", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAWraithIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shapes = scn.GetShadowCard("shapes1");
		var lights = scn.GetShadowCard("lights");
		var nelya = scn.GetShadowCard("nelya");
		// A Wraith in hand is not one to spot.
		scn.MoveCardsToHand(shapes, lights);

		scn.StartGame();
		// A twilight minion of the Ringwraith culture, but a Nazgul and no Wraith.
		scn.MoveMinionsToTable(nelya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 10 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(13, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(shapes));
		assertTrue(scn.ShadowPlayAvailable(lights));

		scn.ShadowPlayCard(lights);
		// 13 - 3 to play - 2 roaming: Corpse Lights is a site 4 minion at site 2
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(shapes));

		scn.ShadowPlayCard(shapes);
		assertInZone(Zone.SUPPORT, shapes);
		assertNotAttachedTo(shapes, lights);
		assertNotAttachedTo(shapes, nelya);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedWithOnlyAWraithCompanionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shapes = scn.GetShadowCard("shapes1");
		// The Free Peoples player's own Wraith: no minion, not twilight, not [sauron], and still a Wraith in play.
		var deadman = scn.GetFreepsCard("deadman");
		scn.MoveCompanionsToTable(deadman);
		scn.MoveCardsToHand(shapes);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 10 set + 2 companions + 2 for the shadow number of site 2
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(shapes));

		scn.ShadowPlayCard(shapes);
		assertInZone(Zone.SUPPORT, shapes);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwilightMinionsCannotBeWoundedInTheManeuverPhaseUntilThereAreOnlyTwoOfThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shapes = scn.GetShadowCard("shapes1");
		var cover = scn.GetShadowCard("cover");
		var lights = scn.GetShadowCard("lights");
		var nelya = scn.GetShadowCard("nelya");
		var attea = scn.GetShadowCard("attea");
		var undead = scn.GetShadowCard("undead");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var defend1 = scn.GetFreepsCard("defend1");
		var defend2 = scn.GetFreepsCard("defend2");
		var stand = scn.GetFreepsCard("stand");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveCardsToHand(defend1, defend2, stand);
		scn.MoveCardsToSupportArea(shapes, cover);

		scn.StartGame();
		// Three twilight minions: a [sauron] Wraith and two [ringwraith] Nazgul.  Beside them a Wraith that is
		// not twilight and a minion that is neither.
		scn.MoveMinionsToTable(lights, nelya, attea, undead, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsPlayCard(defend1);
		// Aragorn is the only [gondor] Man and exerts without a choice.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(undead, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lights, nelya, attea));
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(attea));
		scn.ShadowPass();

		// Corpse Lights is discarded.  Two twilight minions are left, with a twilight condition, a Wraith that
		// is not twilight and a fourth minion beside them: none of those makes a third.
		scn.FreepsPlayCard(stand);
		scn.FreepsChooseCard(lights);
		assertInDiscard(lights);
		assertInZone(Zone.SUPPORT, shapes, cover);
		scn.ShadowPass();

		assertEquals(0, scn.GetWoundsOn(nelya));
		scn.FreepsPlayCard(defend2);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(nelya, attea, undead, troop));
		scn.FreepsChooseCard(nelya);
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(attea));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnArcheryWoundCannotBeAssignedToATwilightMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shapes = scn.GetShadowCard("shapes1");
		var lights = scn.GetShadowCard("lights");
		var nelya = scn.GetShadowCard("nelya");
		var attea = scn.GetShadowCard("attea");
		var undead = scn.GetShadowCard("undead");
		var troop = scn.GetShadowCard("troop");
		// Legolas is the fellowship's one archer.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("legolas"));
		scn.MoveCardsToSupportArea(shapes);

		scn.StartGame();
		scn.MoveMinionsToTable(lights, nelya, attea, undead, troop);
		scn.SkipToArcheryWounds();

		assertEquals(1, scn.GetFreepsArcheryTotal());
		// The Shadow player places the fellowship's archery wound, and may place it only on a minion that can take it.
		assertTrue(scn.ShadowHasCardChoiceAvailable(undead, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lights, nelya, attea));
		assertEquals(0, scn.GetWoundsOn(undead));

		scn.ShadowChooseCard(undead);
		assertEquals(1, scn.GetWoundsOn(undead));
		assertEquals(0, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(attea));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TwilightMinionsTakeWoundsDuringASkirmishAndNoneInTheRegroupPhaseAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shapes = scn.GetShadowCard("shapes1");
		var lights = scn.GetShadowCard("lights");
		var wight = scn.GetShadowCard("wight");
		var nelya = scn.GetShadowCard("nelya");
		var undead = scn.GetShadowCard("undead");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var calaglin = scn.GetFreepsCard("calaglin");
		scn.MoveCompanionsToTable(aragorn, pippin);
		scn.MoveCardsToSupportArea(shapes);

		scn.StartGame();
		scn.MoveMinionsToTable(lights, wight, nelya, undead, troop);
		scn.SkipToAssignments();
		scn.MoveAlliesToTable(calaglin);
		scn.FreepsAssignToMinions(aragorn, lights);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// A twilight minion that is not in this skirmish can be wounded while it is fought.  The roaming minions
		// at site 2 are the three whose site number is 4.
		assertEquals(0, scn.GetWoundsOn(wight));
		scn.FreepsUseCardAction(pippin);
		assertTrue(scn.FreepsHasCardChoiceAvailable(lights, wight, troop));
		scn.FreepsChooseCard(wight);
		assertEquals(2, scn.GetWoundsOn(wight));

		// Aragorn's 8 beats Corpse Lights' 7, and the loser takes its wound.
		assertEquals(0, scn.GetWoundsOn(lights));
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(lights));

		// All three twilight minions are still there for the regroup phase, and the Ring was never put on.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SUPPORT, shapes);
		assertInPlay(lights, wight, nelya);
		assertEquals(0, scn.GetWoundsOn(calaglin));
		scn.FreepsUseCardAction(calaglin);
		assertEquals(1, scn.GetWoundsOn(calaglin));
		assertTrue(scn.FreepsHasCardChoiceAvailable(undead, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lights, wight, nelya));
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(lights));
		assertEquals(2, scn.GetWoundsOn(wight));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void EachCopyIsDiscardedWhenTheRingComesOffAtTheRegroupPhaseAndNotWhenItGoesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shapes1 = scn.GetShadowCard("shapes1");
		var shapes2 = scn.GetShadowCard("shapes2");
		// Another [sauron] condition of the Shadow player's, which is to stay where it is.
		var cover = scn.GetShadowCard("cover");
		var troop = scn.GetShadowCard("troop");
		var ring = scn.GetRing();
		scn.MoveCardsToSupportArea(shapes1, shapes2, cover);

		scn.StartGame();
		// A minion, so that there is a maneuver phase.
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.RBWearingOneRing());
		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		// Putting the Ring on discards nothing.
		assertInZone(Zone.SUPPORT, shapes1, shapes2, cover);

		scn.SkipToPhase(Phase.REGROUP);
		// He wore it "until the regroup phase", and has now taken it off.
		assertFalse(scn.RBWearingOneRing());
		assertInDiscard(shapes1, shapes2);
		assertInZone(Zone.SUPPORT, cover);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DiscardedWhenACardTakesTheRingOffDuringASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shapes = scn.GetShadowCard("shapes1");
		var cover = scn.GetShadowCard("cover");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elbereth = scn.GetFreepsCard("elbereth");
		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, elbereth);
		scn.MoveCardsToSupportArea(shapes, cover);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.RBWearingOneRing());
		assertInZone(Zone.SUPPORT, shapes, cover);

		// Frodo is in no skirmish, so the condition can only take the Ring off.
		scn.FreepsUseCardAction(elbereth);
		assertFalse(scn.RBWearingOneRing());
		assertInDiscard(elbereth);
		assertInDiscard(shapes);
		assertInZone(Zone.SUPPORT, cover);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
