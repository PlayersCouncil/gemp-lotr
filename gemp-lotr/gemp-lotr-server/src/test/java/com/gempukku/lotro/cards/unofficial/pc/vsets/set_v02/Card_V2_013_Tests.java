package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_013_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "102_13");

					// Three companions who are valiant in print, and whose texts do nothing here.
					put("guard", "5_83");        // Household Guard: [rohan] Man; "Valiant" is its whole text
					put("defender", "68_9");     // Elven Defender: [elven] Elf; stronger only beside an [elven] token
					put("recruit", "18_103");    // Rohirrim Recruit: [rohan] Man; stronger only beside a [rohan] token

					// Companions who are not valiant in print.
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability
					put("rider1", "4_265");      // Elite Rider: [rohan] Man; stronger against a wounded minion
					put("rider2", "4_265");
					put("legolas", "1_50");      // Legolas, Greenleaf: an archer, for one wound in the archery phase
					// Ecglaf, Courageous Farmer: a [rohan] Man who is an ally.  A fellowship ability.
					put("ecglaf", "5_81");
					// Northern Signal-fire, Flame of Calenhad: a [rohan] possession of the support area.
					// "Maneuver: Hinder this beacon and exert an unbound Man.  Until the regroup phase, each of
					// your other Men gain each of the unloaded keywords (except race) on that Man."  How an
					// ally comes to be valiant in play: none is in print.
					put("flame", "103_86");

					// Mounts.  Shadowfax goes on Gandalf only; Rider's Mount on a [rohan] Man; Horse of Rohan on
					// a Man, Elf or Wizard.  The two horses make minions exert at the start of their bearer's
					// skirmishes and nothing else.
					put("shadowfax", "4_100");
					put("ridersmount1", "4_287");
					put("ridersmount2", "4_287");
					put("horse", "4_283");
					// Heavy Chain: a [rohan] possession that is not a mount.
					put("chain", "4_278");
					// A Nazgul with nothing to say here, and a mount for him.
					put("lemenya", "1_232");     // Ulaire Lemenya, Lieutenant of Morgul
					put("steed", "1_208");       // Black Steed: stronger at a plains site

					// Gandalf's Staff, Old Man's Crutch: "Response: If Gandalf is exerted by a Free Peoples
					// card, heal him (limit once per turn)."
					put("crutch", "102_77");

					// Minions with special abilities.
					put("taskmaster", "2_90");   // Orc Taskmaster: [sauron] Orc, vitality 3. "Shadow: Exert this minion to add (1)."
					put("grishnakh", "5_100");   // Grishnakh, Orc Captain: [sauron] Orc, vitality 3. "Shadow: Exert
					                             // Grishnakh twice and spot another [sauron] Orc to draw 3 cards.
					                             // The Free Peoples player may add 2 burdens to prevent this."
					put("lurtz", "1_127");       // Lurtz, Servant of Isengard: [isengard] Uruk-hai, vitality 3. "Maneuver:
					                             // Spot another Uruk-hai to make Lurtz fierce until the regroup phase."
					put("saruman", "3_68");      // Saruman, Keeper of Isengard: [isengard] Wizard, vitality 4; takes no
					                             // archery wounds. "Response: If an Uruk-hai is about to take a
					                             // wound, exert Saruman to prevent that wound."
					put("marauder", "10_76");    // Advance Marauder: [sauron] Orc. "Skirmish: Exert this minion to make
					                             // a roaming [sauron] minion strength +3."
					// Orc Inquisitor: "When you play this minion, you may make the Free Peoples player discard a
					// card at random from hand."  Something a minion does that is not a special ability.
					put("inquisitor", "1_268");
					// Mordor Enraged: a condition borne by a [sauron] minion. "Shadow: Exert bearer to add (1)
					// for each archer companion."  A special ability, but the condition's and not the minion's.
					put("enraged", "1_254");

					// Minions with something to do from the discard pile.
					put("sneak", "13_50");       // Gollum, Her Sneak: "Shadow: Remove a card titled Deagol in your discard
					                             // pile from the game to play this minion from your discard pile."
					put("deagol", "13_46");      // Deagol, Fateful Finder: a follower, here only to be removed from the game
					put("deceiver", "7_58");     // Gollum, Plotting Deceiver: cost 2, home site 3. "If you have initiative
					                             // during the Shadow phase, you may play this minion from your
					                             // discard pile."  No phase word: a way to play him, not a special ability.

					put("savage", "1_151");      // Uruk Savage: an Uruk-hai for Lurtz to spot and Saruman to protect
					put("troop", "1_177");       // Goblin Patrol Troop: no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Gandalf, Lathspell
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Theoden
		 * Game Text: Each mounted companion gains <b>valiant</b>.
		 * Each time a minion's special ability is used (except during a skirmish), you may spot 3 valiant
		 * companions and exert Gandalf twice to prevent that and exert that minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("Lathspell", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.THEODEN, card.getBlueprint().getSignet());
	}

	@Test
	public void IsPlayedFromHandAndAMountedCompanionIsValiantOnlyOnceGandalfIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var rider = scn.GetFreepsCard("rider1");
		var mount = scn.GetFreepsCard("ridersmount1");
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, mount);
		scn.MoveCardsToHand(gandalf);

		scn.StartGame();

		assertAttachedTo(mount, rider);
		assertFalse(scn.HasKeyword(rider, Keyword.VALIANT));
		assertTrue(scn.FreepsPlayAvailable(gandalf));

		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.HasKeyword(rider, Keyword.VALIANT));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachCompanionWhoBearsAMountIsValiantAndNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rider1 = scn.GetFreepsCard("rider1");
		var rider2 = scn.GetFreepsCard("rider2");
		var ecglaf = scn.GetFreepsCard("ecglaf");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		var horse = scn.GetFreepsCard("horse");
		var ridersmount1 = scn.GetFreepsCard("ridersmount1");
		var ridersmount2 = scn.GetFreepsCard("ridersmount2");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCompanionsToTable(gandalf, aragorn, rider1, rider2);
		scn.MoveAlliesToTable(ecglaf);
		scn.AttachCardsTo(aragorn, horse);
		scn.AttachCardsTo(rider1, ridersmount1);
		scn.AttachCardsTo(rider2, chain);
		scn.AttachCardsTo(ecglaf, ridersmount2);
		scn.MoveCardsToHand(shadowfax);

		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.AttachCardsTo(lemenya, steed);

		// Mounted companions: a [gondor] Man and a [rohan] Man.
		assertTrue(scn.HasKeyword(aragorn, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(rider1, Keyword.VALIANT));
		// The same [rohan] Man bearing a possession that is not a mount.
		assertFalse(scn.HasKeyword(rider2, Keyword.VALIANT));
		// A mounted ally and a mounted minion: not companions.
		assertAttachedTo(ridersmount2, ecglaf);
		assertFalse(scn.HasKeyword(ecglaf, Keyword.VALIANT));
		assertAttachedTo(steed, lemenya);
		assertFalse(scn.HasKeyword(lemenya, Keyword.VALIANT));
		// Companions bearing nothing but, in Frodo's case, the Ring.
		assertFalse(scn.HasKeyword(frodo, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(gandalf, Keyword.VALIANT));

		// Gandalf is a companion himself: Shadowfax, played on him, makes him valiant too.
		scn.FreepsPlayCard(shadowfax);
		assertAttachedTo(shadowfax, gandalf);
		assertTrue(scn.HasKeyword(gandalf, Keyword.VALIANT));

		assertFalse(scn.HasKeyword(rider2, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(ecglaf, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(lemenya, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(frodo, Keyword.VALIANT));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsTwiceToPreventAMinionsShadowAbilityAndExertThatMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var guard = scn.GetFreepsCard("guard");
		var defender = scn.GetFreepsCard("defender");
		var recruit = scn.GetFreepsCard("recruit");
		// Exactly three valiant companions, and Gandalf, who is not one.
		scn.MoveCompanionsToTable(gandalf, guard, defender, recruit);

		var taskmaster = scn.GetShadowCard("taskmaster");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster, troop);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.HasKeyword(gandalf, Keyword.VALIANT));
		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(taskmaster));
		assertTrue(scn.ShadowActionAvailable(taskmaster));

		scn.ShadowUseCardAction(taskmaster);
		// The Taskmaster's own cost.
		assertEquals(1, scn.GetWoundsOn(taskmaster));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		assertEquals(0, scn.GetWoundsOn(gandalf));

		scn.FreepsChooseOptionalTrigger(gandalf);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(defender));
		assertEquals(0, scn.GetWoundsOn(recruit));
		// The (1) was not added, the exertion that paid for it stands, and the Taskmaster has exerted once more.
		assertEquals(twilight, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(taskmaster));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesGandalfAndTheAbilityAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf, scn.GetFreepsCard("guard"), scn.GetFreepsCard("defender"),
				scn.GetFreepsCard("recruit"));

		var taskmaster = scn.GetShadowCard("taskmaster");

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();

		scn.ShadowUseCardAction(taskmaster);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(twilight + 1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(taskmaster));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnswersEachUseOfAManeuverAbilityAndExertsTheMinionOnceEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf, scn.GetFreepsCard("guard"), scn.GetFreepsCard("defender"),
				scn.GetFreepsCard("recruit"));

		var lurtz = scn.GetShadowCard("lurtz");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz, savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Lurtz's ability costs him nothing, so every wound on him here is Gandalf's doing.
		assertTrue(scn.ShadowActionAvailable(lurtz));
		assertFalse(scn.HasKeyword(lurtz, Keyword.FIERCE));
		assertEquals(0, scn.GetWoundsOn(lurtz));

		scn.ShadowUseCardAction(lurtz);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsChooseOptionalTrigger(gandalf);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertFalse(scn.HasKeyword(lurtz, Keyword.FIERCE));
		assertEquals(1, scn.GetWoundsOn(lurtz));
		assertEquals(0, scn.GetWoundsOn(savage));

		// Gandalf's vitality of 4 pays for one use; his wounds are taken off so that he can pay again.
		scn.RemoveWoundsFromChar(gandalf, 2);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();

		scn.ShadowUseCardAction(lurtz);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsChooseOptionalTrigger(gandalf);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertFalse(scn.HasKeyword(lurtz, Keyword.FIERCE));
		assertEquals(2, scn.GetWoundsOn(lurtz));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void PreventsAnAbilityWhoseOwnCostHasLeftTheMinionUnableToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf, scn.GetFreepsCard("guard"), scn.GetFreepsCard("defender"),
				scn.GetFreepsCard("recruit"));

		var grishnakh = scn.GetShadowCard("grishnakh");
		// Another [sauron] Orc, for Grishnakh to spot.
		var taskmaster = scn.GetShadowCard("taskmaster");

		scn.StartGame();
		scn.MoveMinionsToTable(grishnakh, taskmaster);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(grishnakh));
		assertEquals(0, scn.GetWoundsOn(grishnakh));
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		assertTrue(deck >= 3);
		assertEquals(0, scn.GetBurdens());

		scn.ShadowUseCardAction(grishnakh);
		// Two exertions of his own leave Grishnakh, vitality 3, exhausted.
		assertEquals(2, scn.GetWoundsOn(grishnakh));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));

		scn.FreepsChooseOptionalTrigger(gandalf);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		// Nothing is drawn, and nobody is asked about burdens.  Grishnakh cannot exert again, and does not.
		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(grishnakh));
		assertInZone(Zone.SHADOW_CHARACTERS, grishnakh);
		assertEquals(0, scn.GetWoundsOn(taskmaster));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionsResponseOutsideASkirmishIsASpecialAbilityAndIsPrevented() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		// Legolas is the fellowship's only archer: one archery wound.
		scn.MoveCompanionsToTable(gandalf, scn.GetFreepsCard("guard"), scn.GetFreepsCard("defender"),
				scn.GetFreepsCard("recruit"), scn.GetFreepsCard("legolas"));

		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, savage);
		// Saruman takes no wounds in the archery phase, so the wound can only go to the Uruk Savage.
		scn.SkipToArcheryWounds();

		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));

		scn.ShadowChooseOptionalTrigger(saruman);
		// Saruman's own cost.
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));

		scn.FreepsChooseOptionalTrigger(gandalf);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(saruman));

		assertEquals(0, scn.GetWoundsOn(savage));

		// The wound is still about to be taken, and Saruman, who can still exert, may answer it again.  He
		// does not.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowDeclineOptionalTrigger();

		// His prevention was prevented: the Uruk Savage takes the archery wound after all.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AMinionsAbilityUsedFromTheDiscardPileIsPreventedThoughThereIsNoMinionInPlayToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf, scn.GetFreepsCard("guard"), scn.GetFreepsCard("defender"),
				scn.GetFreepsCard("recruit"));

		var sneak = scn.GetShadowCard("sneak");
		var deagol = scn.GetShadowCard("deagol");
		scn.MoveCardsToDiscard(sneak, deagol);

		scn.StartGame();
		// More than enough to play Gollum.
		scn.SetTwilight(10);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertInZone(Zone.DISCARD, sneak);
		assertInZone(Zone.DISCARD, deagol);
		assertTrue(scn.ShadowActionAvailable(sneak));

		scn.ShadowUseCardAction(sneak);
		// The ability's own cost.
		assertInZone(Zone.REMOVED, deagol);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		assertEquals(0, scn.GetWoundsOn(gandalf));

		scn.FreepsChooseOptionalTrigger(gandalf);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		// Gollum is not played: he is where he was and nothing was paid for him.  Deagol stays removed.
		assertInZone(Zone.DISCARD, sneak);
		assertEquals(twilight, scn.GetTwilight());
		assertInZone(Zone.REMOVED, deagol);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// "Exert that minion" found no minion in play and did nothing, and the game goes on untroubled.  Nothing
		// more is asked of the Free Peoples player, and the Shadow player is not offered Gollum's ability again:
		// it is paid for with a Deagol in the discard pile, and the only one has been removed from the game.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertFalse(scn.ShadowActionAvailable(sneak));

		// The Shadow phase can be passed.  With no minion in play there is nothing to fight, and the regroup
		// phase comes with everything as the prevention left it.
		scn.ShadowPass();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertInZone(Zone.DISCARD, sneak);
		assertInZone(Zone.REMOVED, deagol);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(twilight, scn.GetTwilight());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAMinionIsPlayedFromTheDiscardPileByTextThatIsNoSpecialAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf, scn.GetFreepsCard("guard"), scn.GetFreepsCard("defender"),
				scn.GetFreepsCard("recruit"));

		var deceiver = scn.GetShadowCard("deceiver");
		scn.MoveCardsToDiscard(deceiver);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The Free Peoples player has no cards in hand, so the Shadow has initiative.
		assertTrue(scn.ShadowHasInitiative());
		int twilight = scn.GetTwilight();
		assertInZone(Zone.DISCARD, deceiver);
		assertTrue(scn.ShadowActionAvailable(deceiver));

		scn.ShadowUseCardAction(deceiver);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.SHADOW_CHARACTERS, deceiver);
		// 2 to play + 2 roaming: his home is site 3 and the fellowship is at site 2.
		assertEquals(twilight - 4, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedForASkirmishAbilityUsedDuringASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(gandalf, guard, scn.GetFreepsCard("defender"), scn.GetFreepsCard("recruit"));

		var marauder = scn.GetShadowCard("marauder");

		scn.StartGame();
		scn.MoveMinionsToTable(marauder);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, marauder);
		scn.FreepsPass();

		// The Marauder's home is site 6, so at site 1 it is roaming and may choose itself.
		assertTrue(scn.ShadowActionAvailable(marauder));
		assertEquals(0, scn.GetWoundsOn(marauder));
		assertEquals(9, scn.GetStrength(marauder));

		scn.ShadowUseCardAction(marauder);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		// The ability has happened, and the next skirmish action is the Free Peoples player's.
		assertEquals(1, scn.GetWoundsOn(marauder));
		assertEquals(12, scn.GetStrength(marauder));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithTwoValiantCompanionsAndIsOnceAMountMakesAThird() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var guard = scn.GetFreepsCard("guard");
		var legolas = scn.GetFreepsCard("legolas");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		var ecglaf = scn.GetFreepsCard("ecglaf");
		var flame = scn.GetFreepsCard("flame");
		// Two valiant companions, and two companions who are not: Legolas and Gandalf himself.  Neither is a
		// Man.
		scn.MoveCompanionsToTable(gandalf, guard, scn.GetFreepsCard("defender"), legolas);
		scn.MoveAlliesToTable(ecglaf);
		scn.MoveCardsToSupportArea(flame);

		var lurtz = scn.GetShadowCard("lurtz");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// No ally is valiant in print.  The Flame of Calenhad makes one: the Household Guard, the only unbound
		// Man, exerts, and until the regroup phase the player's other Men have his keyword.  Ecglaf is the
		// only other Man: valiant, and not a companion.
		assertFalse(scn.HasKeyword(ecglaf, Keyword.VALIANT));
		scn.FreepsUseCardAction(flame);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.HasKeyword(ecglaf, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(legolas, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(gandalf, Keyword.VALIANT));

		// Lurtz's ability costs him nothing, and with two valiant companions and a valiant ally nothing
		// answers it.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowUseCardAction(lurtz);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.HasKeyword(lurtz, Keyword.FIERCE));
		assertEquals(0, scn.GetWoundsOn(lurtz));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Shadowfax is put on Gandalf, who is now the third valiant companion by his own text.
		scn.AttachCardsTo(gandalf, shadowfax);
		assertTrue(scn.HasKeyword(gandalf, Keyword.VALIANT));
		scn.FreepsPass();

		scn.ShadowUseCardAction(lurtz);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));

		scn.FreepsChooseOptionalTrigger(gandalf);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(lurtz));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilGandalfCanExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf, scn.GetFreepsCard("guard"), scn.GetFreepsCard("defender"),
				scn.GetFreepsCard("recruit"));
		// Vitality 4 with 2 wounds: he can exert once, not twice.
		scn.AddWoundsToChar(gandalf, 2);

		var taskmaster = scn.GetShadowCard("taskmaster");

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();

		scn.ShadowUseCardAction(taskmaster);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(twilight + 1, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// One wound fewer, and he can.
		scn.RemoveWoundsFromChar(gandalf, 1);

		scn.ShadowUseCardAction(taskmaster);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));

		scn.FreepsChooseOptionalTrigger(gandalf);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(twilight + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotSetOffByAConditionOnAMinionByAMinionsTriggerOrByACompanionsAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCompanionsToTable(gandalf, scn.GetFreepsCard("guard"), scn.GetFreepsCard("defender"),
				scn.GetFreepsCard("recruit"), aragorn);
		// A card for the Inquisitor to discard.
		scn.MoveCardsToHand(chain);

		var taskmaster = scn.GetShadowCard("taskmaster");
		var enraged = scn.GetShadowCard("enraged");
		var inquisitor = scn.GetShadowCard("inquisitor");
		scn.MoveCardsToHand(inquisitor);

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster);
		scn.AttachCardsTo(taskmaster, enraged);
		// Enough for the Inquisitor: 3 to play + 2 roaming.
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A special ability used outside a skirmish, which exerts a minion, but it is the condition's.
		assertTrue(scn.ShadowActionAvailable(enraged));
		assertEquals(0, scn.GetWoundsOn(taskmaster));
		scn.ShadowUseCardAction(enraged);
		assertEquals(1, scn.GetWoundsOn(taskmaster));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The minion's own ability, on the same board, is answered.
		scn.ShadowUseCardAction(taskmaster);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Something a minion does when played is not a special ability.
		assertEquals(1, scn.GetFreepsHandCount());
		scn.ShadowPlayCard(inquisitor);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(inquisitor));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A companion's special ability is not a minion's.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void GandalfsStaffHealsHimBetweenTheTwoExertionsAndTheAbilityIsStillPrevented() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var crutch = scn.GetFreepsCard("crutch");
		scn.MoveCompanionsToTable(gandalf, scn.GetFreepsCard("guard"), scn.GetFreepsCard("defender"),
				scn.GetFreepsCard("recruit"));
		scn.AttachCardsTo(gandalf, crutch);

		var taskmaster = scn.GetShadowCard("taskmaster");

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();

		scn.ShadowUseCardAction(taskmaster);
		assertEquals(1, scn.GetWoundsOn(taskmaster));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gandalf));
		scn.FreepsChooseOptionalTrigger(gandalf);

		// The first of the two exertions is one made by a Free Peoples card, and the Staff answers it before
		// anything else happens.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(taskmaster));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(crutch));
		scn.FreepsChooseOptionalTrigger(crutch);

		// 1 exertion - 1 healed + 1 exertion.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(twilight, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(taskmaster));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
