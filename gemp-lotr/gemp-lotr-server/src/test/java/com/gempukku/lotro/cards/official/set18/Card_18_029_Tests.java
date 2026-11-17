package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_029_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("deceit", "18_29");
					put("deceit2", "18_29");      // a second copy: Deceit is not unique, and each copy is the other's "other"
					// Chasm's Edge: [gollum] Shadow condition in the support area; it strengthens Gollum and nothing else
					put("chasm", "13_44");
					// Treacherous Little Toad: [gollum] Shadow condition borne by a companion, who cannot heal
					put("toad", "12_41");
					// Fat One Wants It: [gollum] Shadow condition; "Regroup: Discard this condition to return Gollum
					// to his owner's hand"
					put("fatone", "7_57");
					put("fear", "1_278");         // Strength Born of Fear: a [sauron] Shadow condition, a skirmish ability only
					put("web", "8_30");           // Web: a [gollum] Shadow possession in the support area
					put("dontlook", "6_39");      // Don't Look at Them: a [gollum] condition of the Free Peoples side

					put("gandalf", "1_364");      // Gandalf, The Grey Wizard: a fellowship ability never used here
					put("sleep", "1_84");         // Sleep, Caradhras: "Fellowship: Exert Gandalf to discard every condition."
					// Galadriel, Lady Redeemed: "Fellowship or Regroup: Discard an [elven] event from hand to discard
					// a Shadow condition or Shadow possession."
					put("galadriel", "10_11");
					put("creation1", "1_44");     // Foul Creation: an [elven] event, for Galadriel to discard
					put("creation2", "1_44");
					put("creation3", "1_44");
					// Arwen, Daughter of Elrond: strength 6, vitality 3; ranger, +3 against a Nazgûl
					put("arwen", "1_30");
					// Hadafang: hand weapon, +2; "Each time bearer wins a skirmish, you may exert him or her to discard a
					// condition from play."
					put("hadafang", "12_18");
					put("runner", "1_178");       // Goblin Runner: strength 5, vitality 1
					// Introspection: "Choose one: Spot a [gandalf] Wizard to choose an opponent who must discard one
					// of his or her conditions from play; or ... at a battleground site ..."
					put("introspection", "12_29");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeceitStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Deceit
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Response:</b> If a Free Peoples player's card is about to discard your other [gollum] condition,
		 * 	remove (1) to prevent that.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("deceit");

		assertEquals("Deceit", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenAFreePeoplesEventDiscardsEveryConditionEachOtherGollumConditionOfItsOwnersMayBeSavedForOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var sleep = scn.GetFreepsCard("sleep");
		var dontlook = scn.GetFreepsCard("dontlook");
		var deceit = scn.GetShadowCard("deceit");
		var chasm = scn.GetShadowCard("chasm");
		var toad = scn.GetShadowCard("toad");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(sleep);
		scn.MoveCardsToSupportArea(deceit, chasm, fear, dontlook);
		scn.AttachCardsTo(gandalf, toad);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, deceit, chasm, fear, dontlook);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertAttachedTo(toad, gandalf);
		scn.FreepsPlayCard(sleep);
		// The event's own 3.
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(gandalf));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(deceit));
		scn.ShadowAcceptOptionalTrigger();
		// One in the support area and one borne by a companion. Not Deceit itself, not the [sauron] condition,
		// and not the [gollum] condition that is the Free Peoples player's own.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chasm, toad));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(deceit, fear, dontlook));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(toad);
		assertEquals(2, scn.GetTwilight());

		// The other is still about to be discarded, and may be answered in its turn for (1) more.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(deceit));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetTwilight());

		assertAttachedTo(toad, gandalf);
		assertInZone(Zone.SUPPORT, chasm);
		assertInDiscard(deceit, fear, dontlook, sleep);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachCopySavesTheOtherAndNotItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var sleep = scn.GetFreepsCard("sleep");
		var deceit = scn.GetShadowCard("deceit");
		var deceit2 = scn.GetShadowCard("deceit2");
		var chasm = scn.GetShadowCard("chasm");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(sleep);
		scn.MoveCardsToSupportArea(deceit, deceit2, chasm);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, deceit, deceit2, chasm);

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(sleep);
		// The event's own 3: one use for each of the three.
		assertEquals(3, scn.GetTwilight());

		// The first copy saves the second, and is not among its own choices.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(deceit));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(deceit2));
		scn.ShadowChooseOptionalTrigger(deceit);
		assertTrue(scn.ShadowHasCardChoiceAvailable(deceit2, chasm));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(deceit));
		scn.ShadowChooseCard(deceit2);
		assertEquals(2, scn.GetTwilight());

		// The second copy saves the first, and is not among its own choices.
		scn.ShadowChooseOptionalTrigger(deceit2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(deceit, chasm));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(deceit2));
		scn.ShadowChooseCard(deceit);
		assertEquals(1, scn.GetTwilight());

		// Chasm's Edge is left, saved for the last (1).
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(deceit));
		scn.ShadowChooseOptionalTrigger(deceit);
		assertEquals(0, scn.GetTwilight());

		assertInZone(Zone.SUPPORT, deceit, deceit2, chasm);
		assertInDiscard(sleep);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenOnlyDeceitItselfASauronConditionAndAFreePeoplesGollumConditionAreDiscardedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var sleep = scn.GetFreepsCard("sleep");
		var dontlook = scn.GetFreepsCard("dontlook");
		var deceit = scn.GetShadowCard("deceit");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(sleep);
		scn.MoveCardsToSupportArea(deceit, fear, dontlook);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, deceit, fear, dontlook);
		scn.SetTwilight(5);
		scn.FreepsPlayCard(sleep);

		// 5 + 3 for the event: the cost could be paid eight times over, and nothing being discarded is
		// another [gollum] condition of the Shadow player's.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(deceit, fear, dontlook, sleep);
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionsAbilityThatDiscardsAGollumConditionIsAnsweredButNotForAGollumPossessionAndNotWithNoTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var creation1 = scn.GetFreepsCard("creation1");
		var creation2 = scn.GetFreepsCard("creation2");
		var creation3 = scn.GetFreepsCard("creation3");
		var deceit = scn.GetShadowCard("deceit");
		var chasm = scn.GetShadowCard("chasm");
		var web = scn.GetShadowCard("web");
		scn.MoveCompanionsToTable(galadriel);
		scn.MoveCardsToHand(creation1, creation2, creation3);
		scn.MoveCardsToSupportArea(deceit, chasm, web);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, deceit, chasm, web);
		assertInHand(creation1, creation2, creation3);
		scn.SetTwilight(1);

		// A [gollum] possession: nothing is offered.
		scn.FreepsUseCardAction(galadriel);
		scn.FreepsChooseCard(creation1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(deceit, chasm, web));
		scn.FreepsChooseCard(web);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(web);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A [gollum] condition: offered, and saved for (1).
		scn.FreepsUseCardAction(galadriel);
		scn.FreepsChooseCard(creation2);
		scn.FreepsChooseCard(chasm);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(deceit));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, chasm);
		// Galadriel's cost stays paid.
		assertInDiscard(creation1, creation2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The same condition with nothing in the pool: nothing is offered and it is discarded.
		scn.FreepsUseCardAction(galadriel);
		scn.FreepsChooseCard(chasm);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(chasm, creation3);
		assertInZone(Zone.SUPPORT, deceit);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void APossessionsTriggerThatDiscardsAGollumConditionIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var hadafang = scn.GetFreepsCard("hadafang");
		var deceit = scn.GetShadowCard("deceit");
		var chasm = scn.GetShadowCard("chasm");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, hadafang);
		scn.MoveCardsToSupportArea(deceit, chasm);

		scn.StartGame();
		assertAttachedTo(hadafang, arwen);
		assertInZone(Zone.SUPPORT, deceit, chasm);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.SetTwilight(2);
		scn.FreepsAssignAndResolve(arwen, runner);
		// Arwen, 6 + 2 for Hadafang, beats 5.
		scn.PassSkirmishActions();

		assertAttachedTo(hadafang, arwen);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hadafang));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(arwen));
		scn.FreepsChooseCard(chasm);

		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(deceit));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, chasm);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AGollumConditionItsOwnerDiscardsAsACostBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deceit = scn.GetShadowCard("deceit");
		var fatone = scn.GetShadowCard("fatone");
		scn.MoveCardsToSupportArea(deceit, fatone);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, deceit, fatone);
		scn.SetTwilight(5);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		assertTrue(pool >= 1);
		assertTrue(scn.ShadowActionAvailable(fatone));
		scn.ShadowUseCardAction(fatone);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(fatone);
		assertInZone(Zone.SUPPORT, deceit);
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenAFreePeoplesEventMakesTheShadowPlayerDiscardHisOwnGollumConditionNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var introspection = scn.GetFreepsCard("introspection");
		var deceit = scn.GetShadowCard("deceit");
		var chasm = scn.GetShadowCard("chasm");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(introspection);
		scn.MoveCardsToSupportArea(deceit, chasm, fear);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, deceit, chasm, fear);
		scn.SetTwilight(3);
		scn.FreepsPlayCard(introspection);
		// 3 + 2 for the event.
		assertEquals(5, scn.GetTwilight());

		// The Shadow player is the one who must discard, and he chooses which of his conditions.
		assertTrue(scn.ShadowHasCardChoiceAvailable(deceit, chasm, fear));
		scn.ShadowChooseCard(chasm);

		// Q-0171: a player with a choice is not forced, and has no protection: nothing is offered.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(chasm);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
