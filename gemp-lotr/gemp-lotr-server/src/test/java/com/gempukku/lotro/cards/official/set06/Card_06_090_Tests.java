package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_090_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("banner", "6_90");
					put("banner2", "6_90");
					// Haethen, Veteran Fighter: [rohan] Man companion, strength 7, vitality 3; cannot take wounds
					// during fierce skirmishes; a skirmish ability used only on purpose
					put("haethen", "12_116");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3; Valiant only
					// Merry, Master Holbytla: a [rohan] Hobbit companion, a Man only while 3 [rohan] companions or
					// Eowyn can be spotted; his own abilities are used only on purpose
					put("merry", "103_85");
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ALLY; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, strength 8, vitality 4

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1; its play trigger is not set off when cheated in
					// Uruk Seeker: fierce, strength 6, vitality 2; its other text is only against a bearer of a search card
					put("seeker", "4_195");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BanneroftheMarkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Banner of the Mark
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Game Text: Bearer must be a [rohan] companion.<br>Each time bearer wins a skirmish, you may exert a minion.<br><b>Response:</b> If bearer wins a skirmish, discard this possession to liberate a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("banner");

		assertEquals("Banner of the Mark", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BearerMustBeARohanCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var guma = scn.GetFreepsCard("guma");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard, merry, aragorn);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(banner);

		scn.StartGame();

		// Two [rohan] companions: Merry is a Hobbit and no Man.
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertInHand(banner);
		assertTrue(scn.FreepsPlayAvailable(banner));
		scn.FreepsPlayCard(banner);

		// A [rohan] Man and a [rohan] Hobbit; not the [rohan] ally, nor companions of other cultures.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guma, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		assertAttachedTo(banner, merry);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeBearerWinsASkirmishHeMayExertAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var haethen = scn.GetFreepsCard("haethen");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(haethen, guard);
		scn.AttachCardsTo(haethen, banner);
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertAttachedTo(banner, haethen);
		scn.MoveMinionsToTable(savage, troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(haethen, savage);
		// The Troop and the Runner are left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(haethen);

		// 7 against 5: Haethen wins, and the Savage takes its wound.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "Exert"));
		scn.FreepsChooseOptionalTrigger(banner, "Exert");

		// Minions of two cultures and races, the one he beat and one in no skirmish; not the Runner (vitality 1,
		// exhausted), nor a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, haethen, guard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(haethen));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertAttachedTo(banner, haethen);
		// The Response to the same win is still on offer.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "liberate"));
	}

	@Test
	public void DecliningTheExertionExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var haethen = scn.GetFreepsCard("haethen");
		scn.MoveCompanionsToTable(haethen);
		scn.AttachCardsTo(haethen, banner);
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(banner, haethen);
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(haethen, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(haethen);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "Exert"));
		// One decline answers both offers of the Banner still open.
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertAttachedTo(banner, haethen);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NeitherIsOfferedWhenBearerLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var haethen = scn.GetFreepsCard("haethen");
		scn.MoveCompanionsToTable(haethen);
		scn.AttachCardsTo(haethen, banner);
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertAttachedTo(banner, haethen);
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(haethen, troop);
		// The Savage, left unassigned, is a minion that could exert.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(haethen);

		// 13 against 7, not doubled: Haethen loses and lives through one wound.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(haethen));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(banner));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertAttachedTo(banner, haethen);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NeitherIsOfferedWhenARohanCompanionWhoIsNotBearerWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var haethen = scn.GetFreepsCard("haethen");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(haethen, guard);
		scn.AttachCardsTo(haethen, banner);
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(banner, haethen);
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);

		// 6 against 5: the Guard, a [rohan] companion the Banner could go on, wins without bearing it.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(banner));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertAttachedTo(banner, haethen);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoMinionLeftThatCanExertOnlyTheResponseIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var haethen = scn.GetFreepsCard("haethen");
		scn.MoveCompanionsToTable(haethen);
		scn.AttachCardsTo(haethen, banner);
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertAttachedTo(banner, haethen);
		scn.MoveMinionsToTable(savage, runner);
		// The Savage dies of the wound for the lost skirmish; the Runner, vitality 1, cannot exert.
		scn.AddWoundsToChar(savage, 2);
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(haethen, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(haethen);
		scn.PassSkirmishActions();

		assertInDiscard(savage);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(banner, "Exert"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "liberate"));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(runner));
		assertAttachedTo(banner, haethen);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerWinningTheFierceSkirmishAsWellMayExertAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var haethen = scn.GetFreepsCard("haethen");
		scn.MoveCompanionsToTable(haethen);
		scn.AttachCardsTo(haethen, banner);
		var seeker = scn.GetShadowCard("seeker");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(banner, haethen);
		scn.MoveMinionsToTable(seeker, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(haethen, seeker);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(haethen);

		// 7 against 6: Haethen wins.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(seeker));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "Exert"));
		scn.FreepsChooseOptionalTrigger(banner, "Exert");
		// The Seeker, at 1 wound of 2, is exhausted: the Troop is exerted unasked.
		assertEquals(1, scn.GetWoundsOn(troop));
		// The Response is declined, and the Banner stays.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "liberate"));
		scn.FreepsDeclineOptionalTrigger();
		assertAttachedTo(banner, haethen);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(haethen, seeker);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(haethen);
		scn.PassFierceSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "Exert"));
		scn.FreepsChooseOptionalTrigger(banner, "Exert");
		assertEquals(2, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "liberate"));
	}

	@Test
	public void ResponseDiscardsTheBannerToLiberateTheHighestSiteAnOpponentControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var banner2 = scn.GetFreepsCard("banner2");
		var haethen = scn.GetFreepsCard("haethen");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(haethen, guard);
		scn.AttachCardsTo(haethen, banner);
		// A second copy, on a [rohan] companion who is in no skirmish.
		scn.AttachCardsTo(guard, banner2);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertAttachedTo(banner, haethen);
		assertAttachedTo(banner2, guard);
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

		scn.MoveMinionsToTable(savage);
		// The Savage dies of the wound for the lost skirmish, so that no minion is left to exert.
		scn.AddWoundsToChar(savage, 2);
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(haethen, savage);
		scn.FreepsResolveSkirmish(haethen);
		scn.PassSkirmishActions();
		assertInDiscard(savage);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "liberate"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(banner2));
		scn.FreepsChooseOptionalTrigger(banner, "liberate");

		assertInDiscard(banner);
		assertAttachedTo(banner2, guard);
		assertFalse(scn.IsSiteControlled(site2));
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site3));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteAnOpponentControlsTheBannerIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetFreepsCard("banner");
		var haethen = scn.GetFreepsCard("haethen");
		scn.MoveCompanionsToTable(haethen);
		scn.AttachCardsTo(haethen, banner);
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertAttachedTo(banner, haethen);
		scn.MoveMinionsToTable(savage);
		// The Savage dies of the wound for the lost skirmish, so that no minion is left to exert.
		scn.AddWoundsToChar(savage, 2);
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(haethen, savage);
		scn.FreepsResolveSkirmish(haethen);
		scn.PassSkirmishActions();
		assertInDiscard(savage);

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "liberate"));
		scn.FreepsChooseOptionalTrigger(banner, "liberate");

		assertInDiscard(banner);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
