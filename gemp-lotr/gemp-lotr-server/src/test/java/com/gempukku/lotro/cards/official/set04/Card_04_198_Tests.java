package com.gempukku.lotro.cards.official.set04;

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

public class Card_04_198_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stormer", "4_198");
					put("sentry", "10_84");     // Cirith Ungol Sentry: a [sauron] Uruk-hai, strength 8; damage +1 is its whole text
					put("forger", "3_56");      // Isengard Forger: an [isengard] Orc, no Uruk-hai, strength 6; a Shadow ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc minion, strength 5, vitality 1; its play is not used
					put("guard", "1_7");        // Dwarf Guard: [dwarven] Dwarf companion, strength 4, vitality 2
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; his ability is not used
					put("merry", "1_303");      // Merry, From O'er the Brandywine: an unbound [shire] Hobbit, vitality 4; bears no weapon
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ALLY, strength 4, vitality 2; takes part in skirmishes by his own maneuver ability
					put("warmed", "4_322");     // Warmed Up a Bit: [shire] Skirmish event, "Discard an unbound Hobbit."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSiteControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stormer", "4_198");
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2; overwhelmed by the Stormer's 8
					put("scout", "15_116");     // Scouting Orc: [orc] Orc, no Uruk-hai; "Skirmish: Exert this minion to wound a companion it is skirmishing."
					put("xbow", "4_183");       // Uruk Crossbowman: Uruk-hai archer; "Archery: Exert this minion to make the Free Peoples player wound an unbound companion ..."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukStormerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk Stormer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>Each time a companion or ally is killed during a skirmish involving an Uruk-hai, you may take control of a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stormer");

		assertEquals("Uruk Stormer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithASiteThatCanBeControlledACompanionKilledInAnUrukHaisSkirmishOffersItAndAYesTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var stormer = scn.GetShadowCard("stormer");
		var guard = scn.GetFreepsCard("guard");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(guard);
		scn.MoveMinionsToTable(stormer);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, stormer);
		scn.FreepsResolveSkirmish(guard);

		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.PassCurrentPhaseActions();
		// 8 against 4: the Guard is overwhelmed.
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(stormer));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledACompanionKilledInAnUrukHaisSkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var stormer = scn.GetShadowCard("stormer");
		var guard = scn.GetFreepsCard("guard");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is the only site every marker has passed, and player 1 took it on player 2's turn: there is
		// nothing a yes could take (Q-0034).
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(guard);
		scn.MoveMinionsToTable(stormer);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, stormer);
		scn.FreepsResolveSkirmish(guard);

		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertTrue(scn.FreepsControls(site1));

		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(stormer));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
	}

	// "Killed during a skirmish involving an Uruk-hai" is any kill in that skirmish, whatever wound dealt it, and
	// not "killed by an Uruk-hai" (CR 4.2's definition of that is for "<X> kills", Q-0328). The card was built
	// with `killer: urukHai`: this kill, by another minion's own wound, was not answered. F-0546.
	@Test
	public void ACompanionKilledInAnUrukHaisSkirmishByAnotherMinionsWoundOffersIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var stormer = scn.GetShadowCard("stormer");
		var scout = scn.GetShadowCard("scout");
		var guard = scn.GetFreepsCard("guard");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(guard);
		scn.AddWoundsToChar(guard, 1);
		scn.MoveMinionsToTable(stormer, scout);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, stormer);
		// The Shadow player puts the [orc] Orc in the same skirmish.
		scn.ShadowAssignToMinions(guard, scout);
		scn.FreepsResolveSkirmish(guard);

		scn.FreepsPass();
		scn.ShadowUseCardAction(scout);
		// The Orc's wound kills the Guard before the skirmish is resolved.
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(stormer));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		// The Guard was the skirmish's only Free Peoples character: it is over, and so is the phase.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// The other side of the same words: an Uruk-hai's own wound outside any skirmish. As built (`killer`, whose
	// `inSkirmish` was not asked of a wound) the archery-phase kill offered a site. F-0546.
	@Test
	public void ACompanionKilledOutsideASkirmishByAnUrukHaisOwnWoundOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var stormer = scn.GetShadowCard("stormer");
		var xbow = scn.GetShadowCard("xbow");
		var guard = scn.GetFreepsCard("guard");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(guard);
		scn.AddWoundsToChar(guard, 1);
		scn.MoveMinionsToTable(stormer, xbow);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		// The Guard is the only unbound companion, and takes the wound with nothing asked.
		scn.ShadowUseCardAction(xbow);
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(stormer));
		assertTrue(scn.FreepsDecisionAvailable("Play Archery action or Pass"));

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
	}

	@Test
	public void EachCompanionKilledInASkirmishInvolvingAnUrukHaiOffersASiteAgainWhateverUrukHaiItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stormer = scn.GetShadowCard("stormer");
		var sentry = scn.GetShadowCard("sentry");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		// Both markers on site 4: sites 1, 2 and 3 are behind every marker as the fellowship moves on to site 5.
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.MoveCompanionsToTable(guard, aragorn);
		// Aragorn has one vitality left.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(stormer, sentry);
		assertEquals(3, scn.GetWoundsOn(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, stormer}, new PhysicalCardImpl[]{aragorn, sentry});
		assertEquals(0, scn.GetShadowControlledSiteCount());

		// A [dwarven] Dwarf, overwhelmed by this minion: 8 against 4.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(stormer));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());

		// A [gondor] Man, killed by the wound for losing to a [sauron] Uruk-hai (8 against 8, the tie to the
		// Shadow), in a skirmish this minion is not in.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(stormer));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowControls(site2));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// "During a skirmish involving an Uruk-hai" is a time (Q-0387, F-0546): a companion in no skirmish, killed while
	// that skirmish is fought, is answered too.
	@Test
	public void ACompanionInNoSkirmishKilledByAThreatWoundDuringTheStormersSkirmishOffersASiteAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stormer = scn.GetShadowCard("stormer");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");

		scn.StartGame();
		// Both markers on site 3: sites 1 and 2 are behind every marker as the fellowship moves on to site 4.
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.MoveCompanionsToTable(guard, merry);
		// Merry has one vitality left, and stays out of the skirmishes.
		scn.AddWoundsToChar(merry, 3);
		scn.AddThreats(1);
		scn.MoveMinionsToTable(stormer);
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetThreats());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, stormer);
		scn.FreepsResolveSkirmish(guard);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		scn.PassSkirmishActions();

		// The Guard is overwhelmed (8 against 4), and the threat comes off as a wound: Merry takes it and is killed.
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.FreepsDecisionAvailable("Choose characters to wound"));
		scn.FreepsChooseCard(merry);
		assertInZone(Zone.DEAD, merry);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(stormer));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(stormer));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyKilledInTheStormersSkirmishOffersASiteAndDecliningLeavesItOnThePath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stormer = scn.GetShadowCard("stormer");
		var thrarin = scn.GetFreepsCard("thrarin");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveMinionsToTable(stormer);

		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in skirmishes.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, stormer);
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();

		// 8 against 4: the ally is overwhelmed.
		assertInZone(Zone.DEAD, thrarin);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(stormer));
		scn.ShadowDeclineOptionalTrigger();
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AKillInASkirmishOfAnOrcAndAMinionKilledInTheStormersSkirmishOfferNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stormer = scn.GetShadowCard("stormer");
		var forger = scn.GetShadowCard("forger");
		var runner = scn.GetShadowCard("runner");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(guard, aragorn);
		// The Guard has one vitality left; Aragorn is strong enough to beat both minions of his skirmish.
		scn.AddWoundsToChar(guard, 1);
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 10));
		scn.MoveMinionsToTable(stormer, forger, runner);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(18, scn.GetStrength(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, forger}, new PhysicalCardImpl[]{aragorn, stormer});
		// The Shadow player puts the Runner beside this minion.
		scn.ShadowAssignToMinions(aragorn, runner);

		// The [isengard] Orc's skirmish first, this minion waiting for his own: 6 against 4, and the wound kills.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(stormer));
		assertFalse(scn.IsSiteControlled(site1));

		// This minion's skirmish: 18 against 8 + 5, and the Runner is killed by its wound. A minion is no companion
		// or ally.
		assertEquals(0, scn.GetWoundsOn(stormer));
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		assertEquals(1, scn.GetWoundsOn(stormer));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(stormer));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
	}

	@Test
	public void ACompanionDiscardedDuringTheStormersSkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stormer = scn.GetShadowCard("stormer");
		var merry = scn.GetFreepsCard("merry");
		var warmed = scn.GetFreepsCard("warmed");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(warmed);
		scn.MoveMinionsToTable(stormer);
		assertInHand(warmed);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, stormer);
		scn.FreepsResolveSkirmish(merry);

		// Merry, the one unbound Hobbit, is discarded from play: not killed.
		scn.FreepsPlayCard(warmed);
		assertInZone(Zone.DISCARD, merry);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(stormer));
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		// Merry was the skirmish's only Free Peoples character: it is over, and so is the phase.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
