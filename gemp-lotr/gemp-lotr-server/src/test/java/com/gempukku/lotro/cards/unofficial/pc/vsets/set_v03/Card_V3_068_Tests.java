package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_068_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legions", "103_68");
					put("hound", "7_192");         // Morgul Hound: [wraith] Orc, (2); its text needs 2 wounds on the Ring-bearer
					put("hound2", "7_192");
					put("mongrel", "7_194");       // Morgul Mongrel: [wraith] Orc, (3); its text needs 3 wounds on the Ring-bearer
					put("whelp", "7_202");         // Morgul Whelp: [wraith] Orc, (1); its text needs a wound on the Ring-bearer
					put("squealer", "8_77");       // Morgul Squealer: [wraith] Orc. Shadow: discard this minion to play a [wraith] minion from your discard pile
					put("runner", "1_178");        // Goblin Runner: [moria] Orc, (1); adds (2) when played
					put("runner2", "1_178");
					put("soldier", "1_271");       // Orc Soldier: [sauron] Orc; a skirmish ability only
					put("lemenya", "1_232");       // Ulaire Lemenya, Lieutenant of Morgul: [wraith] Nazgul, (4); weakens bearers of [wraith] conditions
					put("attea", "103_61");        // Ulaire Attea, Khamul of the East: the one Nazgul who is not [wraith] ([raider]); his text needs 3 burdens
					put("witchking", "1_237");     // The Witch-king, Lord of Angmar: Nazgul, vitality 4; fierce
					put("enquea", "1_231");        // Ulaire Enquea, Lieutenant of Morgul: Nazgul, vitality 4; fierce; his ability needs 6 companions or 5 burdens
					put("spied", "6_84");          // Spied From Above. Regroup: exert a Nazgul twice to play a [wraith] or [sauron] minion; its cost is -8 and it comes into play exhausted

					put("axe", "7_186");           // Morgul Axe: [wraith] possession, (1). Bearer must be a [wraith] Orc
					put("scimitar", "7_203");      // Nazgul Scimitar: [wraith] possession, (1). Bearer must be a [wraith] minion
					put("orcscimitar", "1_269");   // Orc Scimitar: [sauron] possession, (1). Bearer must be a [sauron] Orc
					put("nazgulsword", "1_218");   // Nazgul Sword: [wraith] possession, (1). Bearer must be a Nazgul
					put("intwilight", "19_34");    // In Twilight: [wraith] condition, (2). Bearer must be a [wraith] minion
					put("gorbagsword", "10_60");   // Gorbag's Sword: [wraith] possession. Bearer must be a [wraith] Orc
					put("vileblade", "2_95");      // Vile Blade: [sauron] possession. Bearer must be a [sauron] Orc
					put("pike", "7_265");          // Besieging Pike: [sauron] possession. Bearer must be a [sauron] Orc. It is never taken out of the draw deck
					put("poison", "8_28");         // Spider Poison: Shadow possession a companion bears; bearer exerts when assigned to a skirmish

					put("aragorn", "1_89");        // Aragorn, Ranger of the North
					put("rangersword", "1_112");   // Ranger's Sword: [gondor] possession on Aragorn
					put("hobbitsword", "1_299");   // Hobbit Sword: [shire] possession on a Hobbit; no text
					put("leaf", "1_300");          // Longbottom Leaf: [shire] possession in the support area; a when-played trigger only
					put("shards", "3_44");         // The Shards of Narsil: Free Peoples artifact in the support area; fellowship abilities only
					put("lordofmoria", "1_21");    // Lord of Moria: Free Peoples condition in the support area; each Dwarf is damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulLegionsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Morgul Legions
		 * Unique: true
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: The second time you play a [ringwraith] Orc each Shadow phase, you may hinder a Free Peoples possession.
		 * 	Shadow: Spot a Nazgul to play any number of possessions on your [ringwraith] Orcs from your discard pile.
		 * 	Discard this condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legions");

		assertEquals("Morgul Legions", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedFromHandForOneToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		scn.MoveCardsToHand(legions);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for site 2
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(legions));

		scn.ShadowPlayCard(legions);
		assertEquals(12, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, legions);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheSecondRingwraithOrcPlayedInAShadowPhaseMayHinderAFreePeoplesPossessionAndTheFirstAndThirdMayNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var hound = scn.GetShadowCard("hound");
		var mongrel = scn.GetShadowCard("mongrel");
		var whelp = scn.GetShadowCard("whelp");
		var lemenya = scn.GetShadowCard("lemenya");
		var nazgulsword = scn.GetShadowCard("nazgulsword");
		var poison = scn.GetShadowCard("poison");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var leaf = scn.GetFreepsCard("leaf");
		var shards = scn.GetFreepsCard("shards");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		scn.MoveCardsToSupportArea(legions);
		scn.MoveCardsToHand(hound, mongrel, whelp);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, rangersword, poison);
		scn.AttachCardsTo(frodo, hobbitsword);
		scn.MoveCardsToSupportArea(leaf, shards, lordofmoria);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.AttachCardsTo(lemenya, nazgulsword);
		// A Free Peoples possession that is hindered already.
		scn.HinderCard(hobbitsword);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(hound);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(legions));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(mongrel);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(legions));
		scn.ShadowChooseOptionalTrigger(legions);

		// Offered: a [gondor] possession on a companion and a [shire] one in the support area.
		assertTrue(scn.ShadowHasCardChoiceAvailable(rangersword, leaf));
		// Not offered: the hindered possession, a Free Peoples artifact and condition, and the Shadow
		// possessions on a companion and on a minion.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hobbitsword, shards, lordofmoria, poison, nazgulsword));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(leaf);

		assertTrue(scn.IsHindered(leaf));
		assertFalse(scn.IsHindered(rangersword));
		assertFalse(scn.IsHindered(shards));
		assertFalse(scn.IsHindered(lordofmoria));
		assertFalse(scn.IsHindered(poison));
		assertFalse(scn.IsHindered(nazgulsword));

		scn.ShadowPlayCard(whelp);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(legions));
		assertFalse(scn.IsHindered(rangersword));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningHindersNothingAndTheThirdOrcBringsNoNewOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var hound = scn.GetShadowCard("hound");
		var mongrel = scn.GetShadowCard("mongrel");
		var whelp = scn.GetShadowCard("whelp");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		scn.MoveCardsToSupportArea(legions);
		scn.MoveCardsToHand(hound, mongrel, whelp);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, rangersword);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(hound);
		scn.ShadowPlayCard(mongrel);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(legions));
		scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.IsHindered(rangersword));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(whelp);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(legions));
		assertFalse(scn.IsHindered(rangersword));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesPossessionThatCouldBeHinderedTheSecondOrcBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var hound = scn.GetShadowCard("hound");
		var mongrel = scn.GetShadowCard("mongrel");
		var poison = scn.GetShadowCard("poison");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var shards = scn.GetFreepsCard("shards");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		scn.MoveCardsToSupportArea(legions);
		scn.MoveCardsToHand(hound, mongrel);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, poison);
		scn.AttachCardsTo(frodo, hobbitsword);
		scn.MoveCardsToSupportArea(shards, lordofmoria);

		scn.StartGame();
		// The only Free Peoples possession in play is hindered already; the artifact, the condition and
		// the Shadow possession are not what the text names.
		scn.HinderCard(hobbitsword);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(hound);
		scn.ShadowPlayCard(mongrel);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(legions));
		assertFalse(scn.IsHindered(shards));
		assertFalse(scn.IsHindered(lordofmoria));
		assertFalse(scn.IsHindered(poison));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnlyRingwraithOrcsAreCountedAndOnlyARingwraithOrcIsTheSecond() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var hound = scn.GetShadowCard("hound");
		var mongrel = scn.GetShadowCard("mongrel");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var lemenya = scn.GetShadowCard("lemenya");
		var enquea = scn.GetShadowCard("enquea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		scn.MoveCardsToSupportArea(legions);
		scn.MoveCardsToHand(hound, runner, lemenya, mongrel, runner2, enquea);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, rangersword);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPass();

		// One [wraith] Orc, then a [moria] Orc and a [wraith] Nazgul: still one [wraith] Orc played.
		scn.ShadowPlayCard(hound);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(legions));
		scn.ShadowPlayCard(runner);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(legions));
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(lemenya);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(legions));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The fourth card played is the second [wraith] Orc.
		scn.ShadowPlayCard(mongrel);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(legions));
		scn.ShadowDeclineOptionalTrigger();

		// With two [wraith] Orcs played, a [moria] Orc played next is not "the second".
		scn.ShadowPlayCard(runner2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(legions));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Nor is a [wraith] minion that is no Orc.
		scn.ShadowPlayCard(enquea);
		assertInZone(Zone.SHADOW_CHARACTERS, enquea);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(legions));
		assertFalse(scn.IsHindered(rangersword));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachShadowPhaseHasItsOwnSecondTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var hound = scn.GetShadowCard("hound");
		var hound2 = scn.GetShadowCard("hound2");
		var mongrel = scn.GetShadowCard("mongrel");
		var whelp = scn.GetShadowCard("whelp");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		var leaf = scn.GetFreepsCard("leaf");
		scn.MoveCardsToSupportArea(legions);
		scn.MoveCardsToHand(hound, hound2, mongrel, whelp);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, rangersword);
		scn.MoveCardsToSupportArea(leaf);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(hound);
		scn.ShadowPlayCard(mongrel);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(legions));
		scn.ShadowChooseOptionalTrigger(legions);
		scn.ShadowChooseCard(leaf);
		assertTrue(scn.IsHindered(leaf));

		scn.SkipToMovementDecision();
		scn.SetTwilight(20);
		scn.FreepsChooseToMove();
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The third [wraith] Orc of the turn is the first of this Shadow phase.
		scn.ShadowPlayCard(whelp);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(legions));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The fourth of the turn is the second of this Shadow phase. The Leaf is still hindered, so the
		// Ranger's Sword is the one possession left and is hindered without a choice.
		scn.ShadowPlayCard(hound2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(legions));
		assertFalse(scn.IsHindered(rangersword));
		scn.ShadowChooseOptionalTrigger(legions);
		assertTrue(scn.IsHindered(rangersword));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcPlayedBeforeTheLegionsCameIntoPlayIsTheFirstTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var hound = scn.GetShadowCard("hound");
		var mongrel = scn.GetShadowCard("mongrel");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		var leaf = scn.GetFreepsCard("leaf");
		scn.MoveCardsToHand(hound, legions, mongrel);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, rangersword);
		scn.MoveCardsToSupportArea(leaf);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(hound);
		scn.ShadowPlayCard(legions);
		assertInZone(Zone.SUPPORT, legions);

		// The text counts the times the player plays a [wraith] Orc this phase, not the times this card saw one.
		scn.ShadowPlayCard(mongrel);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(legions));
		scn.ShadowChooseOptionalTrigger(legions);
		assertTrue(scn.ShadowHasCardChoiceAvailable(rangersword, leaf));
		scn.ShadowChooseCard(rangersword);

		assertTrue(scn.IsHindered(rangersword));
		assertFalse(scn.IsHindered(leaf));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcPlayedFromTheDiscardPileByAnotherCardIsPlayedAndCanBeTheSecond() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var squealer = scn.GetShadowCard("squealer");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		var leaf = scn.GetFreepsCard("leaf");
		scn.MoveCardsToSupportArea(legions);
		scn.MoveCardsToHand(squealer);
		scn.MoveCardsToDiscard(hound);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, rangersword);
		scn.MoveCardsToSupportArea(leaf);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(squealer);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(legions));

		// The Squealer discards itself to play the Hound, the only [wraith] minion in the discard pile.
		scn.ShadowUseCardAction(squealer);
		assertInDiscard(squealer);
		assertInZone(Zone.SHADOW_CHARACTERS, hound);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(legions));
		scn.ShadowChooseOptionalTrigger(legions);
		assertTrue(scn.ShadowHasCardChoiceAvailable(rangersword, leaf));
		scn.ShadowChooseCard(leaf);

		assertTrue(scn.IsHindered(leaf));
		assertFalse(scn.IsHindered(rangersword));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwoRingwraithOrcsPlayedInARegroupPhaseBringNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var spied = scn.GetShadowCard("spied");
		var witchking = scn.GetShadowCard("witchking");
		var enquea = scn.GetShadowCard("enquea");
		var hound = scn.GetShadowCard("hound");
		var mongrel = scn.GetShadowCard("mongrel");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		var leaf = scn.GetFreepsCard("leaf");
		scn.MoveCardsToSupportArea(legions, spied);
		scn.MoveCardsToHand(hound, mongrel);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, rangersword);
		scn.MoveCardsToSupportArea(leaf);

		scn.StartGame();
		// Two Nazgul of vitality 4, so that Spied From Above can be paid for twice.
		scn.MoveMinionsToTable(witchking, enquea);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowUseCardAction(spied);
		scn.ShadowChooseCard(witchking);
		scn.ShadowChooseCard(hound);
		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(legions));

		scn.FreepsPass();
		// The Witch-king has one exertion left and cannot exert twice: Enquea is the only Nazgul who can pay,
		// and is taken without asking.  The Mongrel is the only minion left in hand.
		scn.ShadowUseCardAction(spied);
		assertInZone(Zone.SHADOW_CHARACTERS, mongrel);
		assertEquals(2, scn.GetWoundsOn(enquea));

		// The second [wraith] Orc played this phase, and this is a regroup phase, not a Shadow phase.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(legions));
		assertFalse(scn.IsHindered(rangersword));
		assertFalse(scn.IsHindered(leaf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheShadowAbilityIsOfferedOnlyWhileANazgulCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var hound = scn.GetShadowCard("hound");
		var attea = scn.GetShadowCard("attea");
		var axe = scn.GetShadowCard("axe");
		scn.MoveCardsToSupportArea(legions);
		scn.MoveCardsToHand(attea);
		scn.MoveCardsToDiscard(axe);

		scn.StartGame();
		// A [wraith] minion that is no Nazgul, and a bearer for the Axe.
		scn.MoveMinionsToTable(hound);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(legions));

		// The Nazgul who is not [wraith] is played, and the ability is there.
		scn.ShadowPlayCard(attea);
		assertTrue(scn.ShadowActionAvailable(legions));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlaysPossessionsFromTheDiscardPileOntoRingwraithOrcsOneByOneAndIsThenDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var mongrel = scn.GetShadowCard("mongrel");
		var whelp = scn.GetShadowCard("whelp");
		var soldier = scn.GetShadowCard("soldier");
		var axe = scn.GetShadowCard("axe");
		var scimitar = scn.GetShadowCard("scimitar");
		var orcscimitar = scn.GetShadowCard("orcscimitar");
		var nazgulsword = scn.GetShadowCard("nazgulsword");
		var intwilight = scn.GetShadowCard("intwilight");
		var gorbagsword = scn.GetShadowCard("gorbagsword");
		var pike = scn.GetShadowCard("pike");
		var othersVileblade = scn.GetFreepsCard("vileblade");
		scn.MoveCardsToSupportArea(legions);
		scn.MoveCardsToDiscard(axe, scimitar, orcscimitar, nazgulsword, intwilight);
		scn.MoveCardsToHand(gorbagsword);
		scn.MoveCardsToDiscard(othersVileblade);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, hound, mongrel, whelp, soldier);
		// No [sauron] possession can be borne by a [wraith] Orc as printed, so two of the [wraith] Orcs
		// are made [sauron] as well: the Orc Scimitar can then go on them, as it can on the Orc Soldier.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, hound, null, Culture.SAURON));
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, whelp, null, Culture.SAURON));
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for site 2
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(legions));
		scn.ShadowUseCardAction(legions);

		// Offered: two [wraith] possessions and a [sauron] one. Not offered: a possession only a Nazgul
		// bears, a condition a [wraith] minion bears, and possessions in hand, in the draw deck and in
		// the other player's discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, scimitar, orcscimitar));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(nazgulsword, intwilight, gorbagsword, pike, othersVileblade));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertInZone(Zone.SUPPORT, legions);

		// The Nazgul Scimitar's own text would let the Nazgul bear it.
		scn.ShadowChooseCard(scimitar);
		assertTrue(scn.ShadowHasCardChoiceAvailable(hound, mongrel, whelp));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lemenya, soldier));
		scn.ShadowChooseCard(mongrel);
		assertAttachedTo(scimitar, mongrel);
		assertEquals(12, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, legions);

		// The Orc Scimitar's own text would let the [sauron] Orc Soldier bear it. The Mongrel has its hand weapon.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, orcscimitar));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(scimitar, nazgulsword, intwilight));
		scn.ShadowChooseCard(orcscimitar);
		assertTrue(scn.ShadowHasCardChoiceAvailable(hound, whelp));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier, mongrel, lemenya));
		scn.ShadowChooseCard(hound);
		assertAttachedTo(orcscimitar, hound);
		assertEquals(11, scn.GetTwilight());

		// The Whelp is the one [wraith] Orc left without a hand weapon.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(axe);
		assertAttachedTo(axe, whelp);
		assertEquals(10, scn.GetTwilight());

		// Nothing is left that could be played, and the condition is discarded.
		assertInDiscard(legions);
		assertInDiscard(nazgulsword, intwilight);
		assertInHand(gorbagsword);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayingNoPossessionStillDiscardsTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var axe = scn.GetShadowCard("axe");
		scn.MoveCardsToSupportArea(legions);
		scn.MoveCardsToDiscard(axe);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, hound);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var pool = scn.GetTwilight();
		scn.ShadowUseCardAction(legions);
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertInZone(Zone.SUPPORT, legions);
		scn.ShadowDeclineChoosing();

		assertInDiscard(legions);
		assertInDiscard(axe);
		assertNotAttachedTo(axe, hound);
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNothingInTheDiscardPileThatCouldBePlayedOnARingwraithOrcTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legions = scn.GetShadowCard("legions");
		var lemenya = scn.GetShadowCard("lemenya");
		var soldier = scn.GetShadowCard("soldier");
		var hound = scn.GetShadowCard("hound");
		var nazgulsword = scn.GetShadowCard("nazgulsword");
		var orcscimitar = scn.GetShadowCard("orcscimitar");
		var axe = scn.GetShadowCard("axe");
		scn.MoveCardsToSupportArea(legions);
		scn.MoveCardsToHand(hound);
		// Possessions that could each be played, but not on a [wraith] Orc: the Sword goes on the Nazgul, the
		// Orc Scimitar on the [sauron] Orc, and the Axe on a [wraith] Orc, of which there is none yet.
		scn.MoveCardsToDiscard(nazgulsword, orcscimitar, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, soldier);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(legions));

		// A [wraith] Orc is played, and the Axe can now be played on it.
		scn.ShadowPlayCard(hound);

		assertTrue(scn.ShadowActionAvailable(legions));
		assertInZone(Zone.SUPPORT, legions);
		assertInDiscard(nazgulsword, orcscimitar, axe);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
