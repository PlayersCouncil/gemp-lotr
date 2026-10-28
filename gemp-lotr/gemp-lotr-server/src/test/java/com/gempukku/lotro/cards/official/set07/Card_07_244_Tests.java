package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_244_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mustering", "7_244");
					put("mustering2", "7_244");
					put("hguard", "5_83");      // Household Guard: [rohan] Man companion, strength 6, vitality 3; Valiant is its whole text
					// Merry, Master Holbytla: a [rohan] Hobbit companion, vitality 4; a Man only while 3 [rohan]
					// companions (or Eowyn) can be spotted, which is never so here; his texts are used only on purpose
					put("merry", "103_85");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion; his skirmish ability is not used
					// Théoden, Son of Thengel: [rohan] Man companion of vitality 2, so he can exert once and not twice;
					// his regroup ability is not reached
					put("theoden", "4_292");
					// Guma, Plains Farmer and Hlafwine, Village Farmhand: [rohan] Man allies, home 4T (no Fellowship
					// site is theirs); their abilities are used only on purpose
					put("guma", "4_277");
					put("hlafwine", "4_281");

					// [rohan] possessions on "Bearer must be a [rohan] Man", of two classes
					put("spear", "4_288");      // Rider's Spear: hand weapon; damage +1 only if bearer is mounted
					put("mount", "4_287");      // Rider's Mount: mount; its trigger is at the start of a skirmish, never reached
					// Herugrim: a [rohan] ARTIFACT, on Théoden; damage +1
					put("herugrim", "54_280");
					// Brought Down from Inside: a [rohan] CONDITION on a [rohan] Man; its triggers are at the start of a
					// skirmish and at the end of the turn, never reached
					put("brought", "19_24");
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession, "Bearer must be a Hobbit" (Frodo can bear it)
					// Sturdy Shield: a [rohan] possession on a [rohan] Man; laid in the Shadow player's own discard pile
					put("shield", "15_141");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13; no text. There to make a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MusteringforBattleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Mustering for Battle
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 3 [rohan] Men.<br><b>Maneuver:</b> Exert a [rohan] companion twice or discard this condition to play a [rohan] possession from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mustering");

		assertEquals("Mustering for Battle", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilThreeRohanMenAreSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mustering = scn.GetFreepsCard("mustering");
		var hguard = scn.GetFreepsCard("hguard");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var guma = scn.GetFreepsCard("guma");
		var hlafwine = scn.GetFreepsCard("hlafwine");
		scn.MoveCompanionsToTable(hguard, merry, boromir);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(mustering);

		scn.StartGame();

		// Two [rohan] Men, a companion and an ally; Merry is a [rohan] Hobbit (two [rohan] companions make him no
		// Man) and Boromir a [gondor] Man.
		assertInHand(mustering);
		assertInZone(Zone.SUPPORT, guma);
		assertFalse(scn.FreepsPlayAvailable(mustering));

		// A second [rohan] Man ally makes three.
		scn.MoveAlliesToTable(hlafwine);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, hlafwine);
		assertTrue(scn.FreepsPlayAvailable(mustering));

		scn.FreepsPlayCard(mustering);
		assertInZone(Zone.SUPPORT, mustering);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertingARohanCompanionTwicePlaysARohanPossessionFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mustering = scn.GetFreepsCard("mustering");
		var hguard = scn.GetFreepsCard("hguard");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var guma = scn.GetFreepsCard("guma");
		var spear = scn.GetFreepsCard("spear");
		var mount = scn.GetFreepsCard("mount");
		var brought = scn.GetFreepsCard("brought");
		var sword = scn.GetFreepsCard("sword");
		var shadowShield = scn.GetShadowCard("shield");
		scn.MoveCompanionsToTable(hguard, merry, boromir);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToSupportArea(mustering);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCardsToDiscard(spear, mount, brought, sword, shadowShield);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SUPPORT, mustering);
		assertInDiscard(spear, mount, brought, sword, shadowShield);
		assertEquals(0, scn.GetWoundsOn(hguard));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertTrue(scn.FreepsActionAvailable(mustering));
		scn.FreepsUseCardAction(mustering);

		// Both prices can be paid: he is asked which.
		assertTrue(scn.FreepsChoiceAvailable("twice"));
		assertTrue(scn.FreepsChoiceAvailable("Discard this condition"));
		scn.FreepsChoose("twice");

		// The [rohan] companions, a Man and a Hobbit; not the [rohan] ally, nor the [gondor] companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(hguard, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guma, boromir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		// Merry exerts: no other [rohan] Man exerts, so his own trigger is not set off.
		scn.FreepsChooseCard(merry);
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(hguard));
		assertInZone(Zone.SUPPORT, mustering);

		// [rohan] possessions of two classes; not the [rohan] condition or the [shire] possession, each of which
		// could be played there, nor the other player's [rohan] possession in his own discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(spear, mount));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(brought, sword, shadowShield));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(mount);

		// It goes on a [rohan] Man: the Guard or Guma.
		assertTrue(scn.FreepsHasCardChoiceAvailable(hguard, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, boromir));
		scn.FreepsChooseCard(hguard);

		assertAttachedTo(mount, hguard);
		assertInDiscard(spear, brought, sword, shadowShield);
		assertInZone(Zone.SUPPORT, mustering);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DiscardingItPlaysARohanPossessionAndNotARohanArtifact() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mustering = scn.GetFreepsCard("mustering");
		var mustering2 = scn.GetFreepsCard("mustering2");
		var hguard = scn.GetFreepsCard("hguard");
		var theoden = scn.GetFreepsCard("theoden");
		var spear = scn.GetFreepsCard("spear");
		var mount = scn.GetFreepsCard("mount");
		var herugrim = scn.GetFreepsCard("herugrim");
		scn.MoveCompanionsToTable(hguard, theoden);
		// A second copy, not used: it is this copy that is discarded.
		scn.MoveCardsToSupportArea(mustering, mustering2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCardsToDiscard(spear, mount, herugrim);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SUPPORT, mustering, mustering2);
		assertInDiscard(spear, mount, herugrim);
		assertEquals(0, scn.GetWoundsOn(hguard));
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.FreepsActionAvailable(mustering));
		scn.FreepsUseCardAction(mustering);

		assertTrue(scn.FreepsChoiceAvailable("twice"));
		assertTrue(scn.FreepsChoiceAvailable("Discard this condition"));
		scn.FreepsChoose("Discard this condition");

		assertInDiscard(mustering);
		assertInZone(Zone.SUPPORT, mustering2);
		assertEquals(0, scn.GetWoundsOn(hguard));
		assertEquals(0, scn.GetWoundsOn(theoden));

		// Herugrim, a [rohan] artifact that Théoden could bear, is no possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(spear, mount));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(herugrim));
		scn.FreepsChooseCard(spear);
		scn.FreepsChooseCard(theoden);

		assertAttachedTo(spear, theoden);
		assertInDiscard(mount, herugrim, mustering);
		assertInZone(Zone.SUPPORT, mustering2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoRohanCompanionAbleToExertTwiceTheConditionIsDiscardedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mustering = scn.GetFreepsCard("mustering");
		var theoden = scn.GetFreepsCard("theoden");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(theoden);
		scn.MoveCardsToSupportArea(mustering);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCardsToDiscard(spear);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SUPPORT, mustering);
		assertInDiscard(spear);
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.FreepsActionAvailable(mustering));
		scn.FreepsUseCardAction(mustering);

		// Vitality 2: Théoden can exert once, not twice, so only the discard can be paid, and it is paid unasked.
		assertInDiscard(mustering);
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertAttachedTo(spear, theoden);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithNoRohanPossessionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mustering = scn.GetFreepsCard("mustering");
		var hguard = scn.GetFreepsCard("hguard");
		var brought = scn.GetFreepsCard("brought");
		var sword = scn.GetFreepsCard("sword");
		var shadowShield = scn.GetShadowCard("shield");
		scn.MoveCompanionsToTable(hguard);
		scn.MoveCardsToSupportArea(mustering);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCardsToDiscard(brought, sword, shadowShield);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Both prices could be paid, but nothing there can be played: the play is part of what is paid.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInZone(Zone.SUPPORT, mustering);
		assertInDiscard(brought, sword, shadowShield);
		assertEquals(0, scn.GetWoundsOn(hguard));
		assertFalse(scn.FreepsActionAvailable(mustering));
	}

	@Test
	public void NotOfferedWhileTheRohanPossessionInTheDiscardPileHasNoBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mustering = scn.GetFreepsCard("mustering");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(merry, boromir);
		scn.MoveCardsToSupportArea(mustering);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCardsToDiscard(spear);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Merry, a [rohan] companion, could exert twice; but the Spear wants a [rohan] Man, and there is none.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInZone(Zone.SUPPORT, mustering);
		assertInDiscard(spear);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertFalse(scn.FreepsActionAvailable(mustering));
	}
}
