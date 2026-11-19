package com.gempukku.lotro.cards.official.set07;

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

public class Card_07_250_Tests
{

	// The Fellowship path, so that Edoras Hall 7_330 (each [rohan] possession costs 1 less) is not under the play.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("snowmane", "7_250");
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion, strength 5, vitality 3; a skirmish ability used only on purpose
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: [rohan] Man ally; an archery ability that needs a villager
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: [rohan] Hobbit companion; a Man only beside 3 [rohan] companions or Eowyn
					put("rider2", "4_286");     // a second Rider of Rohan (not unique), with no mount: the [rohan] Man companion who is not the bearer
					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("troop4", "1_177");     // the minion that is already exhausted when the bearer's skirmish starts
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					// Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3; its texts are about unbound Hobbits
					put("pursuer", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The regroup ability: Theoden, and a Rider to bear Snowmane in his place.
	protected VirtualTableScenario GetRegroupScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("snowmane", "7_250");
					// Theoden, Rekindled King: [rohan] Man companion, vitality 3; a discount and a fellowship ability used
					// only on purpose
					put("theoden", "7_255");
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SnowmaneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Snowmane
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Game Text: Bearer must be a [rohan] Man.
		 * 	At the start of each skirmish involving bearer, each minion skirmishing bearer must exert.
		 * 	<b>Regroup:</b> If bearer is Théoden, exert him to liberate a site. Any Shadow player may remove (2) to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("snowmane");

		assertEquals("Snowmane", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedOnARohanManCompanionOrAllyAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var snowmane = scn.GetFreepsCard("snowmane");
		var rider = scn.GetFreepsCard("rider");
		var aldor = scn.GetFreepsCard("aldor");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(rider, boromir, merry);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToHand(snowmane);

		scn.StartGame();

		assertInHand(snowmane);
		assertTrue(scn.FreepsPlayAvailable(snowmane));
		scn.FreepsPlayCard(snowmane);
		// A [rohan] Man companion and a [rohan] Man ally; not a [gondor] Man, nor Merry, a [rohan] Hobbit who is no
		// Man beside two [rohan] companions and no Eowyn, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, aldor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, merry, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(rider);
		assertAttachedTo(snowmane, rider);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfBearersSkirmishEachMinionSkirmishingHimExertsAndNoOtherSkirmishDoesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var rider = scn.GetFreepsCard("rider");
		var rider2 = scn.GetFreepsCard("rider2");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		var troop4 = scn.GetShadowCard("troop4");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(rider, rider2);
		scn.AttachCardsTo(rider, snowmane);

		scn.StartGame();
		assertAttachedTo(snowmane, rider);
		// The second Rider is given the strength to live through the Troop: 5 + 4 against 13, which is under double.
		scn.ApplyAdHocModifier(new StrengthModifier(null, rider2, null, 4));
		scn.MoveMinionsToTable(troop1, savage, troop2, troop3, troop4);
		// Vitality 3 with 2 wounds: exhausted, so a wound would kill it where an exertion cannot be paid.
		scn.AddWoundsToChar(troop4, 2);
		assertEquals(2, scn.GetWoundsOn(troop4));
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{rider, troop1}, new PhysicalCardImpl[]{rider2, troop2}, new PhysicalCardImpl[]{frodo, troop3});
		// The Uruk Savage and the exhausted Troop were left over: the Shadow player puts them on the Rider as well.
		scn.ShadowAssignToMinions(rider, savage, troop4);

		// The second Rider's skirmish first, a [rohan] Man with no mount: nobody exerts, those assigned to the bearer included.
		scn.FreepsResolveSkirmish(rider2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(troop3));

		scn.PassSkirmishActions();
		// The second Rider, 9 against 13, loses and takes a wound.
		assertEquals(1, scn.GetWoundsOn(rider2));
		assertEquals(0, scn.GetWoundsOn(troop2));

		// The bearer's skirmish: an Orc and an Uruk-hai, each exerts once; the exhausted Troop beside them is left as it
		// was; the Troop that fought the second Rider does not exert, nor the one assigned to Frodo, whose skirmish has
		// not begun.
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, troop4);
		assertEquals(2, scn.GetWoundsOn(troop4));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(troop3));
	}

	@Test
	public void AMinionSkirmishingBearerAgainInTheFierceRoundExertsAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var rider = scn.GetFreepsCard("rider");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, snowmane);

		scn.StartGame();
		assertAttachedTo(snowmane, rider);
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rider, pursuer);

		assertEquals(0, scn.GetWoundsOn(pursuer));
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(pursuer));

		// 5 against 5: a tie goes to the Shadow side, so the Rider takes a wound and the Pursuer none.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(pursuer));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(rider, pursuer);
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetWoundsOn(pursuer));
	}


	@Test
	public void RegroupAbilityExertsTheodenToLiberateTheHighestSiteAnOpponentControlsWhenTheShadowPlayerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		scn.MoveCompanionsToTable(theoden);
		scn.AttachCardsTo(theoden, snowmane);

		scn.StartGame();
		assertAttachedTo(snowmane, theoden);
		// Both markers on site 3: sites 1 and 2 can be controlled.
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(site1);
		scn.FreepsTakeControlOfSite(site2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site2));
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(snowmane));
		scn.FreepsUseCardAction(snowmane);

		// The cost: Theoden exerts.  The Shadow player, with (2) in the pool, is asked whether to prevent it.
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();

		// Site 1, the only site an opponent controls, goes back on the adventure path; his own site 2 is passed over.
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.FreepsControls(site2));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayRemoveTwoTwilightToPreventTheLiberation() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		scn.MoveCompanionsToTable(theoden);
		scn.AttachCardsTo(theoden, snowmane);

		scn.StartGame();
		assertAttachedTo(snowmane, theoden);
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);

		assertTrue(scn.ShadowControls(site1));
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(theoden));
		scn.FreepsUseCardAction(snowmane);
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseYes();

		// (2) of the (3) is removed and nothing is liberated; Theoden's exertion stays paid.
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithOneTwilightInThePoolTheShadowPlayerIsNotAskedAndTheSiteIsLiberated() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		scn.MoveCompanionsToTable(theoden);
		scn.AttachCardsTo(theoden, snowmane);

		scn.StartGame();
		assertAttachedTo(snowmane, theoden);
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(1);

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetTwilight());
		scn.FreepsUseCardAction(snowmane);

		// (1) cannot pay the (2): no prevention is asked.
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteAnOpponentControlsTheodenExertsForNothingAndNoPreventionIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		scn.MoveCompanionsToTable(theoden);
		scn.AttachCardsTo(theoden, snowmane);

		scn.StartGame();
		assertAttachedTo(snowmane, theoden);
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.FreepsTakeControlOfSite(site1);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(2);

		assertTrue(scn.FreepsControls(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.FreepsActionAvailable(snowmane));
		scn.FreepsUseCardAction(snowmane);

		// Nothing to liberate (his own site is no candidate): the prevention is not put (Q-0114), and the pool keeps its (2).
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsControls(site1));
		assertAttachedTo(snowmane, theoden);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhenBearerIsNotTheodenThoughTheodenIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(theoden, rider);
		scn.AttachCardsTo(rider, snowmane);

		scn.StartGame();
		assertAttachedTo(snowmane, rider);
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		scn.SkipToPhase(Phase.REGROUP);

		// The Rider, a [rohan] Man who can exert, bears it; Theoden is in play beside him, and there is a site to liberate.
		assertTrue(scn.ShadowControls(site1));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(snowmane));
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileTheodenIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRegroupScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		scn.MoveCompanionsToTable(theoden);
		scn.AttachCardsTo(theoden, snowmane);

		scn.StartGame();
		assertAttachedTo(snowmane, theoden);
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(theoden, 2);
		assertEquals(2, scn.GetWoundsOn(theoden));
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetWoundsOn(theoden));
		assertTrue(scn.ShadowControls(site1));
		assertFalse(scn.FreepsActionAvailable(snowmane));
	}
}
