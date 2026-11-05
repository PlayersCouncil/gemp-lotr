package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_013_Tests
{

	// The Hobbit table, with Old Forest Road 30_62 as site 5 in place of Mirkwood 30_53 (which discards every Gandalf
	// as the fellowship moves there): Old Forest Road lets the Shadow player discard Gandalf at the start of the
	// Shadow phase, and nothing else. Elrond 30_21's home is 3H, so at site 5 he takes no part in skirmishes by the
	// ally rule.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hadafang", "33_13");
					// Elrond, Elven Lord: [elven] Elf ally, Wise, home 3H; a start-of-turn heal of a wounded Wise ally
					// and a regroup ability only
					put("elrond", "30_21");
					// Elrond, Elven Lord: an [elven] Elf companion; "To play, spot Aragorn or an Elf" (cheated in) and a
					// start-of-turn heal that exerts him, which no test reaches
					put("elrondcomp", "7_21");
					// Elrond, Lord of Rivendell: the Decipher Elrond ally, home 3F; a start-of-turn heal of allies whose
					// home is site 3 and a fellowship ability only
					put("elrondally", "1_40");
					// Thranduil, Elven King: an [elven] Elf ally who is no Elrond; a maneuver ability only
					put("thranduil", "31_12");
					// Saruman, The White: a Wise [gandalf] ally, home 3H, without Hadafang; defender for Gandalf and two
					// abilities used only on purpose
					put("saruman", "32_24");
					// Gandalf, Friend of Thorin: [gandalf] Wizard companion; Thorin's cost, a fellowship ability only
					put("gandalf", "30_26");
					// Gandalf, The Grey: a second Gandalf; a trigger on [gandalf] events and a fellowship ability only
					put("grey", "30_27");
					// Ent Avenger: a [gandalf] companion who is no Gandalf; +2 only with 3 Ents
					put("avenger", "6_27");
					// Glamdring: [gandalf] artifact, hand weapon, "Bearer must be Gandalf"; a trigger on his wins only
					put("glamdring", "30_29");
					// Glamdring: the Decipher Glamdring, a [gandalf] possession, "Bearer must be Gandalf"; an ability
					// used only on purpose
					put("glamdring1", "1_75");
					// Glamdring, Foe-hammer: another Decipher Glamdring, a [gandalf] possession; "Bearer must be
					// Gandalf", a trigger on his wins only
					put("glamdring11", "11_35");
					// Wizard Staff: a [gandalf] artifact of another title; "Bearer must be a Wizard"
					put("staff", "31_18");
					// Orcrist: a [dwarven] artifact hand weapon of another title; "Bearer must be Thorin"
					put("orcrist", "30_17");
					// Wicked Spider: [spider] minion, cost 2, so that there are skirmishes; a skirmish ability only
					put("spider", "31_55");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.HobbitSites);
					put("site5", "30_62");
				}},
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void HadafangStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Hadafang
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Artifact
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be Elrond.<br>If you cannot spot Gandalf, Elrond may participate in skirmishes at site 5.<br><b>Fellowship:</b> Discard Hadafang to play Glamdring from your draw deck or discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hadafang");

		assertEquals("Hadafang", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void HadafangIsPlayedOnElrondTheAllyAndOnNoOtherElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hadafang = scn.GetFreepsCard("hadafang");
		var elrond = scn.GetFreepsCard("elrondally");
		var thranduil = scn.GetFreepsCard("thranduil");
		scn.MoveAlliesToTable(thranduil);
		scn.MoveCardsToHand(hadafang);

		scn.StartGame();
		assertInZone(Zone.HAND, hadafang);
		// An [elven] Elf ally, and the Ring-bearer, but no Elrond.
		assertFalse(scn.FreepsPlayAvailable(hadafang));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveAlliesToTable(elrond);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(hadafang));
		scn.FreepsPlayCard(hadafang);

		// Elrond is the one bearer it may have: nobody is asked for.
		assertAttachedTo(hadafang, elrond);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HadafangIsPlayedOnElrondTheCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hadafang = scn.GetFreepsCard("hadafang");
		var elrond = scn.GetFreepsCard("elrondcomp");
		var thranduil = scn.GetFreepsCard("thranduil");
		scn.MoveCardsToHand(hadafang);

		scn.StartGame();
		scn.MoveCompanionsToTable(elrond);
		scn.MoveAlliesToTable(thranduil);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.HAND, hadafang);

		assertTrue(scn.FreepsPlayAvailable(hadafang));
		scn.FreepsPlayCard(hadafang);
		assertAttachedTo(hadafang, elrond);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnceGandalfIsDiscardedAtSite5ElrondMayBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hadafang = scn.GetFreepsCard("hadafang");
		var elrond = scn.GetFreepsCard("elrond");
		var saruman = scn.GetFreepsCard("saruman");
		var gandalf = scn.GetFreepsCard("gandalf");
		var spider = scn.GetShadowCard("spider");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveAlliesToTable(elrond, saruman);
		scn.AttachCardsTo(elrond, hadafang);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToDiscard(spider);
		assertAttachedTo(hadafang, elrond);
		assertInZone(Zone.DISCARD, spider);

		// On to Old Forest Road, site 5. Gandalf is in play: Elrond may not be assigned.
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsCanAssign(elrond));
		assertFalse(scn.FreepsCanAssign(saruman));

		// The Shadow player discards Gandalf to play the Spider from his discard pile.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.DISCARD, gandalf);
		assertInZone(Zone.SHADOW_CHARACTERS, spider);

		// No Gandalf: Elrond, who bears Hadafang, may be assigned; Saruman, a Wise ally of the same home without it, may not.
		assertTrue(scn.FreepsCanAssign(elrond));
		assertFalse(scn.FreepsCanAssign(saruman));
		scn.SkipToAssignments();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(elrond));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(saruman));
		scn.FreepsAssignToMinions(elrond, spider);
		assertTrue(scn.IsCharAssigned(elrond));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WhileGandalfStaysAtSite5ElrondMayNotBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hadafang = scn.GetFreepsCard("hadafang");
		var elrond = scn.GetFreepsCard("elrond");
		var grey = scn.GetFreepsCard("grey");
		var spider = scn.GetShadowCard("spider");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, hadafang);
		scn.MoveCompanionsToTable(grey);
		assertAttachedTo(hadafang, elrond);

		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		// No minion in the Shadow player's discard pile: Old Forest Road offers him nothing, and Gandalf stays.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		scn.MoveMinionsToTable(spider);
		assertInZone(Zone.FREE_CHARACTERS, grey);

		assertFalse(scn.FreepsCanAssign(elrond));
		scn.SkipToAssignments();
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(elrond));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(grey));
		assertTrue(scn.FreepsDecisionAvailable("Assign"));
	}

	@Test
	public void ACompanionWhoIsNoGandalfLeavesElrondFreeToBeAssignedAtSite5() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hadafang = scn.GetFreepsCard("hadafang");
		var elrond = scn.GetFreepsCard("elrond");
		var avenger = scn.GetFreepsCard("avenger");
		var spider = scn.GetShadowCard("spider");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, hadafang);
		scn.MoveCompanionsToTable(avenger);
		assertAttachedTo(hadafang, elrond);

		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(spider);

		assertTrue(scn.FreepsCanAssign(elrond));
		scn.SkipToAssignments();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(elrond));
		assertTrue(scn.FreepsDecisionAvailable("Assign"));
	}

	@Test
	public void AtSite4ElrondMayNotBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hadafang = scn.GetFreepsCard("hadafang");
		var elrond = scn.GetFreepsCard("elrond");
		var spider = scn.GetShadowCard("spider");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, hadafang);
		assertAttachedTo(hadafang, elrond);

		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(spider);

		assertFalse(scn.FreepsCanAssign(elrond));
		scn.SkipToAssignments();
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(elrond));
		assertTrue(scn.FreepsDecisionAvailable("Assign"));
	}

	@Test
	public void AtSite6ElrondMayNotBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hadafang = scn.GetFreepsCard("hadafang");
		var elrond = scn.GetFreepsCard("elrond");
		var spider = scn.GetShadowCard("spider");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, hadafang);
		assertAttachedTo(hadafang, elrond);

		scn.FreepsPass();
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(spider);

		assertFalse(scn.FreepsCanAssign(elrond));
		scn.SkipToAssignments();
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(elrond));
		assertTrue(scn.FreepsDecisionAvailable("Assign"));
	}

	@Test
	public void FellowshipActionDiscardsHadafangToPlayGlamdringFromTheDiscardPileOnGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hadafang = scn.GetFreepsCard("hadafang");
		var elrond = scn.GetFreepsCard("elrond");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var glamdring1 = scn.GetFreepsCard("glamdring1");
		var staff = scn.GetFreepsCard("staff");
		var orcrist = scn.GetFreepsCard("orcrist");
		var glamdring11 = scn.GetFreepsCard("glamdring11");
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, hadafang);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToDiscard(glamdring, glamdring11, orcrist);

		scn.StartGame();
		assertAttachedTo(hadafang, elrond);
		assertInZone(Zone.DISCARD, glamdring, glamdring11, orcrist);
		assertInZone(Zone.DECK, glamdring1, staff);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		assertTrue(scn.FreepsActionAvailable(hadafang));
		scn.FreepsUseCardAction(hadafang);
		assertInZone(Zone.DISCARD, hadafang);

		// A Glamdring in each pile: the pile is asked.
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		scn.FreepsChoose("discard pile");
		// The artifact Glamdring and a Decipher possession Glamdring are offered; Orcrist and Hadafang itself are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(glamdring, glamdring11));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(orcrist, hadafang));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(glamdring11);

		assertAttachedTo(glamdring11, gandalf);
		assertInZone(Zone.DECK, glamdring1, staff);
		assertInZone(Zone.DISCARD, glamdring, orcrist);
		// No look at the draw deck.
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoGlamdringInTheDiscardPileTheDrawDeckIsLookedThroughUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hadafang = scn.GetFreepsCard("hadafang");
		var elrond = scn.GetFreepsCard("elrond");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var glamdring1 = scn.GetFreepsCard("glamdring1");
		var staff = scn.GetFreepsCard("staff");
		var orcrist = scn.GetFreepsCard("orcrist");
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, hadafang);
		scn.MoveCompanionsToTable(gandalf);
		scn.SetFreepsDrawDeck(glamdring, glamdring1, staff, orcrist);

		scn.StartGame();
		assertAttachedTo(hadafang, elrond);
		assertInZone(Zone.DECK, glamdring, glamdring1, staff, orcrist);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsUseCardAction(hadafang);
		assertInZone(Zone.DISCARD, hadafang);

		// Nothing to play from the discard pile: no choice of piles.
		scn.FreepsDismissRevealedCards();
		// The artifact Glamdring and a Decipher possession Glamdring; not Wizard Staff or Orcrist.
		assertTrue(scn.FreepsHasCardChoiceAvailable(glamdring, glamdring1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(staff, orcrist));
		scn.FreepsChooseCardBPFromSelection(glamdring);

		assertAttachedTo(glamdring, gandalf);
		assertInZone(Zone.DECK, glamdring1, staff, orcrist);
		// Shuffled as the card leaves the deck looked through, and again after the play.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoGlamdringInTheDrawDeckTheDiscardPileIsUsedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hadafang = scn.GetFreepsCard("hadafang");
		var elrond = scn.GetFreepsCard("elrond");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring1 = scn.GetFreepsCard("glamdring1");
		var staff = scn.GetFreepsCard("staff");
		var orcrist = scn.GetFreepsCard("orcrist");
		var glamdring11 = scn.GetFreepsCard("glamdring11");
		var hisGlamdring = scn.GetShadowCard("glamdring");
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, hadafang);
		scn.MoveCompanionsToTable(gandalf);
		scn.SetFreepsDrawDeck(staff);
		scn.MoveCardsToDiscard(glamdring1, glamdring11, orcrist, hisGlamdring);

		scn.StartGame();
		assertAttachedTo(hadafang, elrond);
		assertInZone(Zone.DECK, staff);
		assertInZone(Zone.DISCARD, glamdring1, glamdring11, orcrist, hisGlamdring);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsUseCardAction(hadafang);
		assertInZone(Zone.DISCARD, hadafang);

		// The draw deck holds no Glamdring: the discard pile is the pile, and nothing is asked of the piles.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		// The other player's Glamdring, in his own discard pile, is not offered; nor is Orcrist.
		assertTrue(scn.FreepsHasCardChoiceAvailable(glamdring1, glamdring11));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(hisGlamdring, orcrist));
		scn.FreepsChooseCard(glamdring1);

		assertAttachedTo(glamdring1, gandalf);
		assertInZone(Zone.DISCARD, glamdring11, hisGlamdring, orcrist);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedWithNoGandalfToBearGlamdring() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hadafang = scn.GetFreepsCard("hadafang");
		var elrond = scn.GetFreepsCard("elrond");
		var glamdring = scn.GetFreepsCard("glamdring");
		var glamdring1 = scn.GetFreepsCard("glamdring1");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, hadafang);
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToDiscard(glamdring1);

		scn.StartGame();
		assertAttachedTo(hadafang, elrond);
		assertInZone(Zone.DECK, glamdring);
		assertInZone(Zone.DISCARD, glamdring1);

		// A Glamdring in each pile, and no Gandalf in play to bear one.
		assertFalse(scn.FreepsActionAvailable(hadafang));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertAttachedTo(hadafang, elrond);
	}

	@Test
	public void FellowshipActionIsNotOfferedWithNoGlamdringInEitherPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hadafang = scn.GetFreepsCard("hadafang");
		var elrond = scn.GetFreepsCard("elrond");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var orcrist = scn.GetFreepsCard("orcrist");
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, hadafang);
		scn.MoveCompanionsToTable(gandalf);
		scn.SetFreepsDrawDeck(staff);
		scn.MoveCardsToDiscard(orcrist);

		scn.StartGame();
		assertAttachedTo(hadafang, elrond);
		assertInZone(Zone.DECK, staff);
		assertInZone(Zone.DISCARD, orcrist);

		assertFalse(scn.FreepsActionAvailable(hadafang));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertAttachedTo(hadafang, elrond);
	}
}
