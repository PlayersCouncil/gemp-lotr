package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

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

public class Card_03_072_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trapped", "53_72");

					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, strength 7, vitality 3; a regroup ability only
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, strength 5, vitality 3; a regroup ability only
					put("retainer", "3_57");    // Isengard Retainer: [isengard] Orc, strength 8, vitality 3; a regroup ability only
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, strength 6; a Shadow ability only
					put("warrior", "4_165");    // Orthanc Warrior: [isengard] Uruk-hai, strength 7, vitality 2, damage +1
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8
					put("sword", "1_112");      // Ranger's Sword: hand weapon on Aragorn, strength +2, damage +1
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, damage +1 of his own
					put("axe", "4_41");         // Axe of Erebor: hand weapon on Gimli, strength +2, damage +1
					put("blow", "1_5");         // Cleaving Blow: Skirmish, a Dwarf strength +2 and damage +1; a damage bonus that is no weapon's
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFellowshipBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trapped", "53_72");
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, strength 7, vitality 3; a regroup ability only
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, strength 6; a Shadow ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Fellowship
		);
	}

	@Test
	public void TrappedAndAloneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Trapped and Alone
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each character skirmishing an [isengard] Orc loses all damage bonuses from weapons.
		 * Skirmish: Spot a skirmishing [Isengard] Orc.  Make the Free Peoples shuffle another [Isengard] Orc into
		 * your draw deck to cancel the active skirmish.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trapped");

		assertEquals("Trapped and Alone", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TrappedAndAloneIsPlayedToTheSupportAreaForOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trapped = scn.GetShadowCard("trapped");
		scn.MoveCardsToHand(trapped);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(trapped));
		int twilight = scn.GetTwilight();

		scn.ShadowPlayCard(trapped);
		assertEquals(twilight - 1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, trapped);
		assertNull(trapped.getAttachedTo());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CharactersSkirmishingAnIsengardOrcLoseOnlyTheirWeaponsDamageBonuses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trapped = scn.GetShadowCard("trapped");
		var servant = scn.GetShadowCard("servant");
		var worker = scn.GetShadowCard("worker");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToSupportArea(trapped);

		scn.StartGame();
		scn.MoveMinionsToTable(servant, worker);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, servant}, new PhysicalCardImpl[]{gimli, worker});
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		scn.FreepsResolveSkirmish(aragorn);
		// In his skirmish Aragorn loses the sword's damage +1.  Gimli is assigned to an [isengard] Orc too, but his
		// skirmish has not begun, and he keeps both.
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// Strength 10 beats 7: one wound and no more.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		scn.FreepsResolveSkirmish(gimli);
		// Gimli's own damage +1 is not from a weapon and stays; the axe's goes.
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// Strength 8 beats 5: 1 wound + his own damage +1.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(worker));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADamageBonusThatDoesNotComeFromAWeaponIsKeptInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trapped = scn.GetShadowCard("trapped");
		var servant = scn.GetShadowCard("servant");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var blow = scn.GetFreepsCard("blow");

		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToSupportArea(trapped);
		scn.MoveCardsToHand(blow);

		scn.StartGame();
		scn.MoveMinionsToTable(servant);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, servant);

		// The axe's damage +1 is lost; Gimli's own stays.
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(servant));

		scn.FreepsPlayCard(blow);
		// The event's damage +1 comes from no weapon, so it is kept: 1 of his own + 1 from the event, not the axe's.
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Strength 6 + 2 from the axe + 2 from the event is 10 and beats 7: 1 wound + damage 2 is 3, the Orc's vitality.
		scn.PassSkirmishActions();
		assertInDiscard(servant);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsLostOrOfferedInSkirmishesAgainstAnIsengardUrukHaiOrASauronOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trapped = scn.GetShadowCard("trapped");
		var warrior = scn.GetShadowCard("warrior");
		var soldier = scn.GetShadowCard("soldier");
		var forger = scn.GetShadowCard("forger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToSupportArea(trapped);

		scn.StartGame();
		// The Forger is an [isengard] Orc that could be shuffled in, were the ability offered.  It is assigned to
		// the Ring-bearer, and that skirmish is not begun, so it is no skirmishing Orc.
		scn.MoveMinionsToTable(warrior, soldier, forger);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, warrior}, new PhysicalCardImpl[]{gimli, soldier});
		scn.ShadowAssignToMinions(scn.GetRingBearer(), forger);
		scn.FreepsResolveSkirmish(aragorn);

		// Against an [isengard] Uruk-hai.
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(trapped));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Strength 10 beats 7: 1 wound + the sword's damage +1 kills the Warrior.
		scn.ShadowPass();
		assertInDiscard(warrior);

		// Against a [sauron] Orc.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(trapped));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityMakesTheFreePeoplesPlayerShuffleAnotherIsengardOrcToCancelTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trapped = scn.GetShadowCard("trapped");
		var servant = scn.GetShadowCard("servant");
		var retainer = scn.GetShadowCard("retainer");
		var forger = scn.GetShadowCard("forger");
		var warrior = scn.GetShadowCard("warrior");
		var soldier = scn.GetShadowCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(trapped);

		scn.StartGame();
		scn.MoveMinionsToTable(servant, retainer, forger, warrior, soldier);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, servant}, new PhysicalCardImpl[]{gimli, retainer});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, servant));
		assertTrue(scn.ShadowActionAvailable(trapped));
		int shadowDeck = scn.GetShadowDeckCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.ShadowUseCardAction(trapped);

		// The Free Peoples player chooses: another [isengard] Orc, in a skirmish yet to come or in none; not the
		// skirmishing one, not an [isengard] Uruk-hai, not a [sauron] Orc.
		assertTrue(scn.FreepsHasCardChoiceAvailable(forger, retainer));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(servant, warrior, soldier));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(forger);

		assertInZone(Zone.DECK, forger);
		assertEquals(shadowDeck + 1, scn.GetShadowDeckCount());
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());

		// The skirmish is cancelled: nobody is wounded and the next skirmish is chosen.
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertFalse(scn.IsCharAssigned(servant));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertInZone(Zone.SHADOW_CHARACTERS, retainer);
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void ASecondIsengardOrcInTheSameSkirmishMayBeShuffledInButNotTheOneSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trapped = scn.GetShadowCard("trapped");
		var servant = scn.GetShadowCard("servant");
		var worker = scn.GetShadowCard("worker");
		var forger = scn.GetShadowCard("forger");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(trapped);

		scn.StartGame();
		scn.MoveMinionsToTable(servant, worker, forger);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, servant);
		scn.ShadowAssignToMinions(aragorn, worker);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, servant));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, worker));
		assertTrue(scn.ShadowActionAvailable(trapped));

		scn.ShadowUseCardAction(trapped);
		// Either skirmishing Orc may be the one spotted.
		assertTrue(scn.ShadowHasCardChoiceAvailable(servant, worker));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger));
		scn.ShadowChooseCard(servant);

		// "Another" is any Orc but the one spotted: the other skirmishing Orc and the one in no skirmish are offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(worker, forger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(servant));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(worker);

		assertInZone(Zone.DECK, worker);
		assertInZone(Zone.SHADOW_CHARACTERS, servant);
		assertInZone(Zone.SHADOW_CHARACTERS, forger);
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoOtherIsengardOrcToShuffleIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trapped = scn.GetShadowCard("trapped");
		var servant = scn.GetShadowCard("servant");
		var warrior = scn.GetShadowCard("warrior");
		var soldier = scn.GetShadowCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(trapped);

		scn.StartGame();
		// Beside the skirmishing Orc, an [isengard] minion that is no Orc and an Orc that is not [isengard].
		scn.MoveMinionsToTable(servant, warrior, soldier);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, servant);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, servant));
		assertFalse(scn.ShadowActionAvailable(trapped));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InFellowshipBlockCancelsASkirmishTheRingBearerIsIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var trapped = scn.GetShadowCard("trapped");
		var servant = scn.GetShadowCard("servant");
		var forger = scn.GetShadowCard("forger");
		var frodo = scn.GetRingBearer();

		scn.MoveCardsToSupportArea(trapped);

		scn.StartGame();
		scn.MoveMinionsToTable(servant, forger);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, servant);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, servant));
		assertTrue(scn.ShadowActionAvailable(trapped));
		scn.ShadowUseCardAction(trapped);

		assertInZone(Zone.DECK, forger);
		assertFalse(scn.IsCharSkirmishing(servant));
		assertFalse(scn.IsCharAssigned(frodo));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InMultipathShufflesTheOrcInAndDoesNotCancelASkirmishTheRingBearerIsIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trapped = scn.GetShadowCard("trapped");
		var servant = scn.GetShadowCard("servant");
		var forger = scn.GetShadowCard("forger");
		var frodo = scn.GetRingBearer();

		scn.MoveCardsToSupportArea(trapped);

		scn.StartGame();
		scn.MoveMinionsToTable(servant, forger);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, servant);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, servant));
		// The cost can be paid, so the ability is offered; the cancel is what the format refuses.
		assertTrue(scn.ShadowActionAvailable(trapped));
		scn.ShadowUseCardAction(trapped);

		assertInZone(Zone.DECK, forger);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, servant));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
