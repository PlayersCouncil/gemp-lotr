package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_141_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shield", "15_141");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3; Valiant is its whole text
					// Guma, Plains Farmer: [rohan] Man ally, strength 4, home 4T (discarded only if an opponent controls
					// site 4 of a Towers path); his fellowship ability is used only on purpose
					put("guma", "4_277");
					// Merry, Master Holbytla: a [rohan] Hobbit companion; a Man only while 3 [rohan] companions (or
					// Éowyn) can be spotted, which is never so where he is laid; his texts are used only on purpose
					put("merry", "103_85");
					put("boromir", "0_65");     // Boromir, Steward's Heir: [gondor] Man companion; keywords only
					// Éomer, Forthwith Banished: [rohan] Man companion; valiant, damage +1 and a cost reduction are his whole text
					put("eomer", "0_66");
					// Firefoot: [rohan] mount on a [rohan] Man; its play trigger is withheld with no [rohan] token in
					// play, and its maneuver trigger needs Éomer to bear it and 2 [rohan] tokens
					put("firefoot", "13_126");
					// Southron Explorer: [raider] Man, strength 5, vitality 3; "Regroup: Exert this minion and remove (5)
					// to take control of a site"
					put("explorer", "4_250");
					// Heavy Chain: [rohan] armor on a Man, a possession that is no mount; its text is a bar on overwhelming
					put("chain", "4_278");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its only text is on its play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SturdyShieldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Sturdy Shield
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Shield
		 * Game Text: Bearer must be a [rohan] Man.<br>While no opponent controls a site, bearer is strength +1.<br><b>Regroup:</b> Discard this to liberate a site (or two sites if bearer is mounted).
		*/

		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");

		assertEquals("Sturdy Shield", shield.getBlueprint().getTitle());
		assertNull(shield.getBlueprint().getSubtitle());
		assertFalse(shield.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, shield.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, shield.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, shield.getBlueprint().getCardType());
		assertTrue(shield.getBlueprint().getPossessionClasses().contains(PossessionClass.SHIELD));
		assertEquals(2, shield.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedOnARohanManItMakesHimStrengthPlus1WhileNoOpponentControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard, merry, boromir);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(shield);

		scn.StartGame();

		assertInHand(shield);
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(guma));

		scn.FreepsPlayCard(shield);
		// [rohan] Men: a companion and an ally.  Not Merry (a [rohan] Hobbit, no Man beside two [rohan] companions),
		// Boromir (a [gondor] Man) or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, boromir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);
		assertAttachedTo(shield, guard);

		// No site is controlled by anyone: 6 + 1.  Guma, who does not bear it, is unchanged.
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(guma));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASiteOfHisOwnKeepsTheBonusAndOneAnOpponentTakesEndsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var guma = scn.GetFreepsCard("guma");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guma, shield);

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		// The only controlled site is the Free Peoples player's own: no opponent controls one.
		scn.FreepsTakeControlOfSite(site1);
		assertTrue(scn.FreepsControls(site1));
		assertAttachedTo(shield, guma);
		assertEquals(5, scn.GetStrength(guma));

		scn.MoveMinionsToTable(explorer);
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(5, scn.GetStrength(guma));

		scn.SetTwilight(5);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertTrue(scn.ShadowActionAvailable(explorer));
		scn.ShadowUseCardAction(explorer);
		// The Explorer takes site 2, the lowest site nobody controls behind every marker.
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertTrue(scn.ShadowControls(site2));

		assertEquals(4, scn.GetStrength(guma));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsItToLiberateTheHighestSiteAnOpponentControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var guard = scn.GetFreepsCard("guard");
		var eomer = scn.GetFreepsCard("eomer");
		var firefoot = scn.GetFreepsCard("firefoot");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCompanionsToTable(guard, eomer);
		// The bearer bears a possession that is no mount, and another companion is mounted.
		scn.AttachCardsTo(guard, shield, chain);
		scn.AttachCardsTo(eomer, firefoot);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		// The Shadow player controls sites 1 and 2, and the Free Peoples player the highest, site 3.
		scn.ShadowTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		scn.FreepsTakeControlOfSite(site3);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.FreepsControls(site3));
		assertAttachedTo(shield, guard);
		assertAttachedTo(chain, guard);
		assertAttachedTo(firefoot, eomer);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsActionAvailable(shield));
		scn.FreepsUseCardAction(shield);

		assertInDiscard(shield);
		// One site, the highest an opponent controls, with nothing asked; his own site 3 is passed over.
		assertFalse(scn.IsSiteControlled(site2));
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site3));
		assertAttachedTo(chain, guard);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithItsBearerMountedItLiberatesTwoSites() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var guard = scn.GetFreepsCard("guard");
		var firefoot = scn.GetFreepsCard("firefoot");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, shield, firefoot);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		scn.ShadowTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		scn.FreepsTakeControlOfSite(site3);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.FreepsControls(site3));
		assertAttachedTo(shield, guard);
		assertAttachedTo(firefoot, guard);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsActionAvailable(shield));
		scn.FreepsUseCardAction(shield);

		assertInDiscard(shield);
		// Both of the opponent's sites; his own site 3 is passed over.
		assertFalse(scn.IsSiteControlled(site1));
		assertFalse(scn.IsSiteControlled(site2));
		assertInZone(Zone.ADVENTURE_PATH, site1, site2);
		assertTrue(scn.FreepsControls(site3));
		assertAttachedTo(firefoot, guard);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteAnOpponentControlsItIsOfferedAndLiberatesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, shield);

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		// The only controlled site is his own player's.
		scn.FreepsTakeControlOfSite(site1);
		assertTrue(scn.FreepsControls(site1));
		assertAttachedTo(shield, guard);
		scn.SkipToPhase(Phase.REGROUP);

		// Discarding the Shield is a cost and can be paid, so the ability is there to use.
		assertTrue(scn.FreepsActionAvailable(shield));
		scn.FreepsUseCardAction(shield);

		assertInDiscard(shield);
		assertTrue(scn.FreepsControls(site1));
		assertInZone(Zone.SUPPORT, site1);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, shield);

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		assertTrue(scn.ShadowControls(site1));
		assertAttachedTo(shield, guard);
		// A minion, so that there is a maneuver phase.
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(shield));
		assertTrue(scn.ShadowControls(site1));
		assertAttachedTo(shield, guard);
	}
}
