package com.gempukku.lotro.cards.unofficial.pc.errata.set18;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_029_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("deceit", "68_29");
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
					// Introspection: "Choose one: Spot a [gandalf] Wizard to choose an opponent who must discard one
					// of his or her conditions from play; or ... at a battleground site ..."
					put("introspection", "12_29");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetHeightsOfIsengardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("deceit", "68_29");
					put("chasm", "13_44");        // Chasm's Edge: [gollum] Shadow condition in the support area
					put("gandalf", "1_364");      // Gandalf, The Grey Wizard: [gandalf] companion, strength 7
					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3; damage +1 is its whole text
				}},
				new HashMap<>()
				{{
					// Fangorn Glade: adds twilight for Free Peoples weapons when the fellowship moves to it
					put("site1", "11_239");
					// Heights of Isengard: "Each time a [gandalf] companion wins a skirmish, the Free Peoples
					// player may discard a condition from play."
					put("site2", "11_244");
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void DeceitStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Deceit
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Response:</b> If the Free Peoples player is about to discard your other [gollum] condition,
		 * 	remove (2) to prevent that.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("deceit");

		assertEquals("Deceit", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenAFreePeoplesEventDiscardsEveryConditionEachOtherGollumConditionOfItsOwnersMayBeSavedForTwo() throws DecisionResultInvalidException, CardNotFoundException {
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
		scn.SetTwilight(1);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsPlayCard(sleep);
		// 1 + 3 for the event: enough for two uses and no more.
		assertEquals(4, scn.GetTwilight());
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

		// The other is still about to be discarded, and may be answered in its turn with exactly (2).
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(deceit));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetTwilight());

		assertAttachedTo(toad, gandalf);
		assertInZone(Zone.SUPPORT, chasm);
		assertInDiscard(deceit, fear, dontlook, sleep);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTwilightForOneUseOnlyTheSecondGollumConditionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var sleep = scn.GetFreepsCard("sleep");
		var deceit = scn.GetShadowCard("deceit");
		var chasm = scn.GetShadowCard("chasm");
		var toad = scn.GetShadowCard("toad");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(sleep);
		scn.MoveCardsToSupportArea(deceit, chasm);
		scn.AttachCardsTo(gandalf, toad);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(sleep);
		// The event's own 3.
		assertEquals(3, scn.GetTwilight());

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(deceit));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(chasm);
		assertEquals(1, scn.GetTwilight());

		// (1) is not (2): nothing more is offered, and the Toad goes with Deceit.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, chasm);
		assertInDiscard(toad, deceit, sleep);
		assertEquals(1, scn.GetTwilight());
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
		scn.SetTwilight(5);
		scn.FreepsPlayCard(sleep);

		// 5 + 3 for the event: the cost could be paid four times over, and nothing being discarded is
		// another [gollum] condition of the Shadow player's.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(deceit, fear, dontlook, sleep);
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionsAbilityThatDiscardsAGollumConditionIsAnsweredButNotForAGollumPossessionAndNotWithOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
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
		scn.SetTwilight(3);

		// A [gollum] possession: nothing is offered.
		scn.FreepsUseCardAction(galadriel);
		scn.FreepsChooseCard(creation1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(deceit, chasm, web));
		scn.FreepsChooseCard(web);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(web);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A [gollum] condition: offered, and saved for (2).
		scn.FreepsUseCardAction(galadriel);
		scn.FreepsChooseCard(creation2);
		scn.FreepsChooseCard(chasm);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(deceit));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, chasm);
		// Galadriel's cost stays paid.
		assertInDiscard(creation1, creation2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The same condition with (1) in the pool: nothing is offered and it is discarded.
		scn.FreepsUseCardAction(galadriel);
		scn.FreepsChooseCard(chasm);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(chasm, creation3);
		assertInZone(Zone.SUPPORT, deceit);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGollumConditionItsOwnerDiscardsAsACostBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deceit = scn.GetShadowCard("deceit");
		var fatone = scn.GetShadowCard("fatone");
		scn.MoveCardsToSupportArea(deceit, fatone);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		assertTrue(pool >= 2);
		assertTrue(scn.ShadowActionAvailable(fatone));
		scn.ShadowUseCardAction(fatone);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(fatone);
		assertInZone(Zone.SUPPORT, deceit);
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenTheFreePeoplesPlayerDiscardsAGollumConditionByASitesTextItMayBeSaved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHeightsOfIsengardScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var deceit = scn.GetShadowCard("deceit");
		var chasm = scn.GetShadowCard("chasm");
		var savage = scn.GetShadowCard("savage");
		var heights = scn.GetShadowSite("site2");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(deceit, chasm);

		scn.StartGame(scn.GetFreepsSite("site1"));

		// The Shadow player lays the next site, and the fellowship moves to it.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(heights);
		assertEquals("Heights of Isengard", scn.GetCurrentSite().getBlueprint().getTitle());

		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(4);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gandalf, savage);
		// 7 against 5: a [gandalf] companion wins, and the site lets the Free Peoples player discard a condition.
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(deceit, chasm));
		scn.FreepsChooseCard(chasm);

		// It is the Free Peoples player who is discarding, though by no card of his own.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(deceit));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, chasm);
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
		scn.SetTwilight(3);
		scn.FreepsPlayCard(introspection);
		// 3 + 2 for the event.
		assertEquals(5, scn.GetTwilight());

		// The Shadow player is the one who must discard, and he chooses which of his conditions.
		assertTrue(scn.ShadowHasCardChoiceAvailable(deceit, chasm, fear));
		scn.ShadowChooseCard(chasm);

		// Q-0171 (open): it is not the Free Peoples player who discards, so nothing is offered.  Under the other
		// reading Deceit would be offered here and Chasm's Edge saved for (2).
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(chasm);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
