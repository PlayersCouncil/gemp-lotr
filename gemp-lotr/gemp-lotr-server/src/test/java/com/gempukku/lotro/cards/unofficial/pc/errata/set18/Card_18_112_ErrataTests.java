package com.gempukku.lotro.cards.unofficial.pc.errata.set18;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_112_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scouring", "68_112");

					// [shire] conditions and possessions, in the support area and borne.
					put("promise", "2_112");      // A Promise: a [shire] condition in the support area; it strengthens Frodo and Sam
					put("tale", "1_317");         // There and Back Again: a [shire] condition a Hobbit companion bears
					put("sword", "1_299");        // Hobbit Sword: a [shire] possession a Hobbit bears, strength +2
					put("toby", "1_305");         // Old Toby: a [shire] possession in the support area
					// Golden Perch Ale: a [shire] possession in the support area that carries [shire] tokens of its own
					put("ale", "18_108");
					// [shire] cards that are neither conditions nor possessions.
					put("coat", "13_153");        // Mithril-coat, Dwarf-mail: a [shire] artifact a Ring-bound Hobbit bears
					put("farmer", "15_147");      // Hobbiton Farmer: a [shire] follower; bearer is strength +1
					// A possession and a condition of other Free Peoples cultures.
					put("cloak", "1_42");         // Elven Cloak: an [elven] possession a companion bears
					put("lord", "1_21");          // Lord of Moria: a [dwarven] condition in the support area; it concerns Dwarves only
					// Bill the Pony, Dearly-loved: "When you play Bill the Pony, you may reinforce a [shire] token."
					put("pony", "13_143");

					put("sam", "1_311");          // Sam, Son of Hamfast: strength 3, vitality 4, Ring-bound
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8
					put("gandalf", "1_364");      // Gandalf, The Grey Wizard: a fellowship ability never used here
					put("sleep", "1_84");         // Sleep, Caradhras: "Fellowship: Exert Gandalf to discard every condition."

					// Shadow events that discard, and the minions they need.
					// Beauty Is Fading: "Maneuver: Exert a Nazgul to discard a Free Peoples possession or Free Peoples condition."
					put("beauty1", "1_205");
					put("beauty2", "1_205");
					put("nelya", "2_84");         // Ulaire Nelya, Ringwraith in Twilight: vitality 3; nothing else below 2 burdens
					put("power", "1_136");        // Saruman's Power: "Shadow: Exert an [isengard] minion to discard all conditions."
					put("savage", "1_151");       // Uruk Savage: an [isengard] minion, vitality 3
					put("fear", "1_278");         // Strength Born of Fear: a [sauron] condition in the Shadow support area
					// Ravage the Defeated: "Response: If your [dunland] Man wins a skirmish, discard all Free Peoples cards
					// borne by the companion or ally he was skirmishing."
					put("ravage", "4_32");
					put("brigand", "4_10");       // Dunlending Brigand: a [dunland] Man, strength 8, no text
					// Storming the Ramparts: "Spot an [orc] minion to make the Free Peoples player discard one of his or
					// her conditions from play."
					put("storming", "12_103");
					put("goblin", "11_137");      // Scurrying Goblin: an [orc] minion; its text needs an underground site
					// Hand of Sauron: "Maneuver: Exert a [sauron] minion to make the Free Peoples player discard a card at
					// random from his or her hand."
					put("hand", "3_90");
					put("soldier", "1_271");      // Orc Soldier: a [sauron] minion, vitality 2; a skirmish ability only

					// Shadow cards in play that discard.
					put("club", "4_36");          // War Club: "Each time bearer wins a skirmish, you may discard 2 Free Peoples possessions."
					put("smith", "3_60");         // Isengard Smith: "Regroup: Exert this minion to discard a weapon"

					put("troop", "1_177");        // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Two Shadows-block sites on a Fellowship-block path, which the turn-skippers can walk. They stand in each
	// adventure deck in place of sites 7 and 8, which no test reaches, and a test puts the Shadow player's copy of
	// one of them on the path as site 2.
	protected VirtualTableScenario GetShadowsSitesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scouring", "68_112");
					put("promise", "2_112");      // A Promise: a [shire] condition in the support area
					put("toby", "1_305");         // Old Toby: a [shire] possession in the support area
					put("nelya", "2_84");         // Ulaire Nelya, Ringwraith in Twilight: site number 2, so not roaming there
					put("beauty", "1_205");       // Beauty Is Fading
					put("sam", "1_311");          // Sam, Son of Hamfast: left in the draw deck
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site7");
					remove("site8");
					// Buckland Homestead: "Shadow: Play a Nazgul to discard a condition from play."
					put("bucklandSite", "11_230");
					// Redhorn Pass: "Culture tokens cannot be added, removed, or reinforced."
					put("redhorn", "13_194");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ScouringOfTheShireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Scouring of the Shire
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Tale.</b>
		 * 	When there are 3 [shire] tokens here, discard this condition from play.
		 * 	<b>Response:</b> If a [shire] condition or [shire] possession is about to be discarded from play by a
		 * 	Shadow card, add a [shire] token here to prevent that.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scouring");

		assertEquals("Scouring of the Shire", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedFromHandForOneToTheSupportAreaWithNoTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouring = scn.GetFreepsCard("scouring");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(scouring);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(scouring));

		scn.FreepsPlayCard(scouring);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, scouring);
		assertNotAttachedTo(scouring, frodo);
		assertEquals(0, scn.GetCultureTokensOn(scouring));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShirePossessionAShadowEventDiscardsIsSavedForATokenAndAnElvenPossessionIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouring = scn.GetFreepsCard("scouring");
		var toby = scn.GetFreepsCard("toby");
		var cloak = scn.GetFreepsCard("cloak");
		var sam = scn.GetFreepsCard("sam");
		var nelya = scn.GetShadowCard("nelya");
		var beauty1 = scn.GetShadowCard("beauty1");
		var beauty2 = scn.GetShadowCard("beauty2");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(scouring, toby);
		scn.AttachCardsTo(sam, cloak);
		scn.MoveCardsToHand(beauty1, beauty2);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);

		scn.SkipToPhase(Phase.MANEUVER);
		// Two copies of Beauty Is Fading at (5) each.
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(beauty1));
		scn.ShadowPlayCard(beauty1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(scouring, toby, cloak));
		scn.ShadowChooseCard(toby);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scouring));
		assertEquals(0, scn.GetCultureTokensOn(scouring));
		assertInZone(Zone.SUPPORT, toby);

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(scouring));
		assertInZone(Zone.SUPPORT, toby);
		// What the Shadow player paid stays paid.
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.DISCARD, beauty1);
		// Nothing is about to be discarded any more: no second offer.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(scouring));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The second copy is aimed at the Elven Cloak Sam bears: no offer, and it goes.
		scn.FreepsPass();
		scn.ShadowPlayCard(beauty2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(scouring, toby, cloak));
		scn.ShadowChooseCard(cloak);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(scouring));
		assertInZone(Zone.DISCARD, cloak);
		assertEquals(1, scn.GetCultureTokensOn(scouring));
		assertInZone(Zone.SUPPORT, scouring, toby);
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WhenAShadowEventDiscardsAllConditionsEachShireConditionMayBeSavedForATokenOfItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouring = scn.GetFreepsCard("scouring");
		var promise = scn.GetFreepsCard("promise");
		var tale = scn.GetFreepsCard("tale");
		var lord = scn.GetFreepsCard("lord");
		var sam = scn.GetFreepsCard("sam");
		var savage = scn.GetShadowCard("savage");
		var power = scn.GetShadowCard("power");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(scouring, promise, lord);
		scn.AttachCardsTo(sam, tale);
		scn.MoveCardsToSupportArea(fear);
		scn.MoveCardsToHand(power);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(2);

		scn.SkipToPhase(Phase.SHADOW);

		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(power));
		scn.ShadowPlayCard(power);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(pool - 2, scn.GetTwilight());

		// Five conditions are about to go. The three [shire] ones may be saved, this one among them; the
		// [dwarven] one and the Shadow player's own may not.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scouring));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(promise, tale, scouring));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lord, fear));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(tale);
		assertEquals(1, scn.GetCultureTokensOn(scouring));

		// Offered again for what is still about to go.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scouring));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(promise, scouring));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(tale, lord, fear));
		scn.FreepsChooseCard(scouring);
		assertEquals(2, scn.GetCultureTokensOn(scouring));

		// And a third time, for A Promise: declined, so it goes with the rest.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scouring));
		scn.FreepsDeclineOptionalTrigger();

		assertAttachedTo(tale, sam);
		// Two tokens are not three: it stays.
		assertInZone(Zone.SUPPORT, scouring);
		assertEquals(2, scn.GetCultureTokensOn(scouring));
		assertInZone(Zone.DISCARD, promise, lord, fear, power);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(pool - 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OfEverythingACompanionBearsThatAShadowEventDiscardsOnlyTheShirePossessionAndConditionMayBeSaved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouring = scn.GetFreepsCard("scouring");
		var sword = scn.GetFreepsCard("sword");
		var tale = scn.GetFreepsCard("tale");
		var coat = scn.GetFreepsCard("coat");
		var farmer = scn.GetFreepsCard("farmer");
		var cloak = scn.GetFreepsCard("cloak");
		var sam = scn.GetFreepsCard("sam");
		var brigand = scn.GetShadowCard("brigand");
		var ravage = scn.GetShadowCard("ravage");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(scouring);
		scn.AttachCardsTo(sam, sword, tale, coat, farmer, cloak);
		scn.MoveCardsToHand(ravage);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);

		scn.SkipToAssignments();
		// 3 + 2 for the Hobbit Sword + 1 for the Hobbiton Farmer, against the Brigand's 8: Sam loses and lives.
		assertEquals(6, scn.GetStrength(sam));
		scn.FreepsAssignAndResolve(sam, brigand);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));

		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(ravage));
		scn.ShadowPlayCard(ravage);
		assertEquals(pool - 1, scn.GetTwilight());

		// All five cards Sam bears are about to go. The artifact and the follower are [shire] cards of other
		// types, and the Cloak a possession of another culture.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scouring));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, tale));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(coat, farmer, cloak));
		scn.FreepsChooseCard(sword);
		assertEquals(1, scn.GetCultureTokensOn(scouring));

		// Again, for the Tale: the only card left that can be saved, so there is no choice to make.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scouring));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(scouring));

		assertAttachedTo(sword, sam);
		assertAttachedTo(tale, sam);
		assertInZone(Zone.DISCARD, coat, farmer, cloak);
		// Two tokens put here by its own response: it stays.
		assertInZone(Zone.SUPPORT, scouring);
		assertEquals(2, scn.GetCultureTokensOn(scouring));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AShadowPossessionsDiscardOfAShireAndAnElvenPossessionIsAnsweredForTheShireOneOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouring = scn.GetFreepsCard("scouring");
		var sword = scn.GetFreepsCard("sword");
		var toby = scn.GetFreepsCard("toby");
		var cloak = scn.GetFreepsCard("cloak");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var brigand = scn.GetShadowCard("brigand");
		var club = scn.GetShadowCard("club");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToSupportArea(scouring, toby);
		scn.AttachCardsTo(sam, sword);
		scn.AttachCardsTo(aragorn, cloak);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.AttachCardsTo(brigand, club);

		scn.SkipToAssignments();
		// The Brigand's 8 + 2 for the War Club, against Aragorn's 8.
		scn.FreepsAssignAndResolve(aragorn, brigand);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The War Club's own text does the discarding: a Shadow card that is no character.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(club));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, toby, cloak));
		scn.ShadowChooseCards(sword, cloak);

		// Of the two, only the Hobbit Sword can be saved, so there is no choice to make.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scouring));
		assertEquals(0, scn.GetCultureTokensOn(scouring));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(scouring));

		assertAttachedTo(sword, sam);
		assertInZone(Zone.DISCARD, cloak);
		assertInZone(Zone.SUPPORT, toby, scouring);
		assertEquals(1, scn.GetCultureTokensOn(scouring));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMinionsAbilityThatDiscardsAShirePossessionIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouring = scn.GetFreepsCard("scouring");
		var sword = scn.GetFreepsCard("sword");
		var sam = scn.GetFreepsCard("sam");
		var smith = scn.GetShadowCard("smith");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(scouring);
		scn.AttachCardsTo(sam, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(smith);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// "Exert this minion to discard a weapon": the Hobbit Sword is the only weapon in play.
		assertTrue(scn.ShadowActionAvailable(smith));
		scn.ShadowUseCardAction(smith);
		assertEquals(1, scn.GetWoundsOn(smith));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scouring));
		assertEquals(0, scn.GetCultureTokensOn(scouring));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(scouring));
		assertAttachedTo(sword, sam);
		assertEquals(1, scn.GetWoundsOn(smith));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AShadowEventThatMakesTheFreePeoplesPlayerDiscardHisOwnShireConditionIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouring = scn.GetFreepsCard("scouring");
		var promise = scn.GetFreepsCard("promise");
		var tale = scn.GetFreepsCard("tale");
		var sam = scn.GetFreepsCard("sam");
		var goblin = scn.GetShadowCard("goblin");
		var storming = scn.GetShadowCard("storming");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(scouring, promise);
		scn.AttachCardsTo(sam, tale);
		scn.MoveCardsToHand(storming);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(storming));
		scn.ShadowPlayCard(storming);
		// The Free Peoples player picks which of his conditions is to go. A Shadow card is still what discards it.
		assertTrue(scn.FreepsHasCardChoiceAvailable(scouring, promise, tale));
		scn.FreepsChooseCard(promise);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scouring));
		assertEquals(0, scn.GetCultureTokensOn(scouring));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(scouring));
		assertInZone(Zone.SUPPORT, promise, scouring);
		assertAttachedTo(tale, sam);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DISCARD, storming);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AFreePeoplesEventThatDiscardsEveryConditionBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouring = scn.GetFreepsCard("scouring");
		var promise = scn.GetFreepsCard("promise");
		var tale = scn.GetFreepsCard("tale");
		var sam = scn.GetFreepsCard("sam");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sleep = scn.GetFreepsCard("sleep");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCompanionsToTable(sam, gandalf);
		scn.MoveCardsToSupportArea(scouring, promise);
		scn.AttachCardsTo(sam, tale);
		scn.MoveCardsToSupportArea(fear);
		scn.MoveCardsToHand(sleep);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(sleep));
		scn.FreepsPlayCard(sleep);
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// Sleep, Caradhras is a Free Peoples card: nothing is offered and every condition goes, this one too.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, scouring, promise, tale, fear);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShadowPlayerDiscardingAShireConditionByASitesTextBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsSitesScenario();

		var scouring = scn.GetFreepsCard("scouring");
		var promise = scn.GetFreepsCard("promise");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToSupportArea(scouring, promise);
		scn.MoveCardsToHand(nelya);

		scn.StartGame();
		// Buckland Homestead becomes site 2.
		scn.MoveCardToAdventurePath(scn.GetShadowSite("bucklandSite"));
		scn.SetTwilight(10);
		scn.FreepsPass();

		var homestead = scn.GetCurrentSite();
		assertEquals("Buckland Homestead", homestead.getBlueprint().getTitle());
		assertTrue(scn.ShadowActionAvailable(homestead));

		// "Shadow: Play a Nazgul to discard a condition from play." The Shadow player does it, by a site.
		scn.ShadowUseCardAction(homestead);
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertTrue(scn.ShadowHasCardChoiceAvailable(scouring, promise));
		scn.ShadowChooseCard(promise);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(scouring));
		assertInZone(Zone.DISCARD, promise);
		assertInZone(Zone.SUPPORT, scouring);
		assertEquals(0, scn.GetCultureTokensOn(scouring));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AShireConditionDiscardedAsItsOwnCostOrFromHandByAShadowCardBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouring = scn.GetFreepsCard("scouring");
		var promise = scn.GetFreepsCard("promise");
		var tale = scn.GetFreepsCard("tale");
		var sam = scn.GetFreepsCard("sam");
		var soldier = scn.GetShadowCard("soldier");
		var hand = scn.GetShadowCard("hand");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(scouring);
		scn.AttachCardsTo(sam, tale);
		scn.MoveCardsToHand(promise);
		scn.MoveCardsToHand(hand);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);

		scn.SkipToPhase(Phase.MANEUVER);

		// "Maneuver: Discard this condition to make each Hobbit companion strength +2": discarded from play,
		// by its own owner's card.
		assertTrue(scn.FreepsActionAvailable(tale));
		scn.FreepsUseCardAction(tale);
		assertInZone(Zone.DISCARD, tale);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(scouring));
		assertEquals(0, scn.GetCultureTokensOn(scouring));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Hand of Sauron takes the one card in the Free Peoples player's hand, A Promise: discarded by a
		// Shadow card, and not from play.
		assertEquals(1, scn.GetFreepsHandCount());
		scn.ShadowPlayCard(hand);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertInZone(Zone.DISCARD, promise);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(scouring));
		assertEquals(0, scn.GetCultureTokensOn(scouring));
		assertInZone(Zone.SUPPORT, scouring);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WhereNoTokenCanBeAddedTheResponseIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsSitesScenario();

		var scouring = scn.GetFreepsCard("scouring");
		var toby = scn.GetFreepsCard("toby");
		var nelya = scn.GetShadowCard("nelya");
		var beauty = scn.GetShadowCard("beauty");
		scn.MoveCardsToSupportArea(scouring, toby);
		scn.MoveCardsToHand(beauty);

		scn.StartGame();
		// Redhorn Pass becomes site 2: "Culture tokens cannot be added, removed, or reinforced."
		scn.MoveCardToAdventurePath(scn.GetShadowSite("redhorn"));
		scn.FreepsPass();
		scn.MoveMinionsToTable(nelya);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		assertEquals("Redhorn Pass", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.FreepsPass();

		// The same play that is answered at Ettenmoors: Beauty Is Fading aimed at Old Toby.
		assertTrue(scn.ShadowPlayAvailable(beauty));
		scn.ShadowPlayCard(beauty);
		assertTrue(scn.ShadowHasCardChoiceAvailable(scouring, toby));
		scn.ShadowChooseCard(toby);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(scouring));
		assertInZone(Zone.DISCARD, toby);
		assertEquals(0, scn.GetCultureTokensOn(scouring));
		assertInZone(Zone.SUPPORT, scouring);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AShirePossessionThatGoesWithItsDyingBearerBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouring = scn.GetFreepsCard("scouring");
		var sword = scn.GetFreepsCard("sword");
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(scouring);
		scn.AttachCardsTo(sam, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		// 3 + 2 for the Hobbit Sword, against 13: Sam is overwhelmed and what he bears is discarded with him.
		scn.FreepsAssignAndResolve(sam, troop);
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, sam);
		assertInZone(Zone.DISCARD, sword);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(scouring));
		assertEquals(0, scn.GetCultureTokensOn(scouring));
		assertInZone(Zone.SUPPORT, scouring);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheThirdTokenStillSavesTheCardAndThenThisConditionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouring = scn.GetFreepsCard("scouring");
		var toby = scn.GetFreepsCard("toby");
		var ale = scn.GetFreepsCard("ale");
		var nelya = scn.GetShadowCard("nelya");
		var beauty1 = scn.GetShadowCard("beauty1");
		scn.MoveCardsToSupportArea(scouring, toby, ale);
		scn.MoveCardsToHand(beauty1);

		scn.StartGame();
		scn.AddTokensToCard(scouring, 2);
		scn.AddTokensToCard(ale, 2);
		scn.MoveMinionsToTable(nelya);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);

		// Two tokens here and two more on the Ale beside it, which are not "here": it is still in play.
		assertInZone(Zone.SUPPORT, scouring);
		assertEquals(2, scn.GetCultureTokensOn(scouring));
		assertEquals(2, scn.GetCultureTokensOn(ale));

		scn.FreepsPass();
		scn.ShadowPlayCard(beauty1);
		scn.ShadowChooseCard(toby);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scouring));

		scn.FreepsAcceptOptionalTrigger();
		// The token was added and Old Toby is saved; with its third token the condition is discarded.
		assertInZone(Zone.SUPPORT, toby);
		assertInZone(Zone.DISCARD, scouring);
		assertInZone(Zone.SUPPORT, ale);
		assertEquals(2, scn.GetCultureTokensOn(ale));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AThirdTokenPutHereByAnotherCardDiscardsThisCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouring = scn.GetFreepsCard("scouring");
		var ale = scn.GetFreepsCard("ale");
		var pony = scn.GetFreepsCard("pony");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(scouring, ale);
		scn.MoveCardsToHand(pony);

		scn.StartGame();
		scn.AddTokensToCard(scouring, 2);
		scn.AddTokensToCard(ale, 1);

		// Bill the Pony, Dearly-loved goes on a Ring-bound Hobbit and may reinforce a [shire] token.
		assertTrue(scn.FreepsPlayAvailable(pony));
		scn.FreepsPlayCard(pony);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(sam);
		assertInZone(Zone.SUPPORT, scouring);
		assertEquals(2, scn.GetCultureTokensOn(scouring));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pony));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(scouring, ale));
		scn.FreepsChooseCard(scouring);

		assertInZone(Zone.DISCARD, scouring);
		assertInZone(Zone.SUPPORT, ale);
		assertEquals(1, scn.GetCultureTokensOn(ale));
		assertAttachedTo(pony, sam);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
