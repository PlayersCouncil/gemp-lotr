package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_079_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("anduril", "7_79");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					// Aragorn's Bow: a ranged weapon, cost 1.  "Bearer must be Aragorn."
					put("bow", "1_90");

					// What restores Anduril by legal play:
					// Mantle of the White Wizard, [gandalf] condition in the support area: "Each time you
					// play a spell, you may hinder a [gandalf] artifact to restore a Free Peoples card."
					put("mantle", "103_3");
					// Gandalf, Friend of the Shirefolk, bearing Gandalf's Staff, a [gandalf] artifact ("The
					// twilight cost of each [gandalf] spell is -1")
					put("gandalf", "1_72");
					put("staff", "2_22");
					// A New Light: [gandalf] Fellowship event, a spell, cost 1.  To play, spot a [gandalf]
					// Wizard.  It takes a minion from the Shadow player's discard pile, where there is none.
					put("light", "17_15");

					// Aragorn, Defender of Rohan: the [rohan] Aragorn.  Valiant, and a hunter bonus only while the
					// Ring-bearer is assigned.  Never in play beside the other Aragorn.
					put("rohanaragorn", "17_93");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man who is no Aragorn; a skirmish ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, Knight and nothing else
					// Gondor Bow: a ranged weapon, cost 1, for a [gondor] Man; its skirmish ability needs a fortification
					put("gondorbow", "7_94");
					// Ring of Barahir: an artifact that is no weapon, cost 0, "Bearer must be Aragorn"; an optional
					// heal at the start of each turn that needs a culture token
					put("barahir", "6_55");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AndurilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Andúril, Flame of the West
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Artifact
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Vitality: 1
		 * Game Text: Bearer must be Aragorn. Discard other weapons he bears. He is <b>damage +1</b> and cannot bear other weapons.<br><b>Fellowship</b> <i>or</i> <b>Regroup:</b> If the fellowship is at any site 2 or any site 5, play the fellowship's next site (replacing opponent's site if necessary).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("anduril");

		assertEquals("Andúril", card.getBlueprint().getTitle());
		assertEquals("Flame of the West", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	// "Discard other weapons he bears" is a standing discard, apart from "cannot bear other weapons" (Ketura on
	// Q-0223 and Q-0229: "two separate, unrelated effects that just so happen to share a target"; the discard is
	// continuous).  While Anduril's text is in force the bar keeps every other weapon from reaching Aragorn, by a
	// play or by a transfer, so the discard is seen at two moments only: as Anduril is played on an Aragorn who
	// bears a weapon, here, and as it is restored, in the test after.
	@Test
	public void WhenPlayedDiscardsTheWeaponAragornBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(aragorn);
		// A ranged weapon: no hand weapon, so the item class rule lets Anduril be played beside it
		scn.AttachCardsTo(aragorn, bow);
		scn.MoveCardsToHand(anduril);

		scn.StartGame();

		assertAttachedTo(bow, aragorn);
		assertTrue(scn.FreepsPlayAvailable(anduril));
		scn.FreepsPlayCard(anduril);

		assertAttachedTo(anduril, aragorn);
		assertInZone(Zone.DISCARD, bow);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// Comprehensive Rules 5.0, "hinder": "If Anduril is hindered, a Ranger's Sword cannot be played on Aragorn due
	// to passive item class restrictions.  But because Anduril's game text does not apply, Aragorn's Bow can now
	// be played.  Upon being restored, Anduril's game text resumes being applied, upon which Aragorn's Bow would
	// be discarded."  Here a card restores it; mechanics/hinder/HinderedBorneCardTests has the same board with a
	// reconcile doing the restoring.
	@Test
	public void WhenRestoredDiscardsAWeaponAragornTookUpWhileItWasHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var mantle = scn.GetFreepsCard("mantle");
		var light = scn.GetFreepsCard("light");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.AttachCardsTo(aragorn, anduril);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToSupportArea(mantle);
		scn.MoveCardsToHand(bow, light);

		scn.StartGame();

		// "Cannot bear other weapons"
		assertFalse(scn.FreepsPlayAvailable(bow));

		scn.HinderCard(anduril);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(bow));
		scn.FreepsPlayCard(bow);
		assertAttachedTo(bow, aragorn);

		// The spell is played, and the Mantle hinders the Staff (the one [gandalf] artifact) to restore Anduril
		scn.FreepsPlayCard(light);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mantle));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.IsHindered(staff));
		assertTrue(scn.FreepsHasCardChoiceAvailable(anduril));
		scn.FreepsChooseCard(anduril);

		assertFalse(scn.IsHindered(anduril));
		assertAttachedTo(anduril, aragorn);
		assertInZone(Zone.DISCARD, bow);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void GoesOnAragornAndNotBoromirAndMakesAragornAloneDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gondorbow = scn.GetFreepsCard("gondorbow");
		var barahir = scn.GetFreepsCard("barahir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(anduril);

		scn.StartGame();
		// Laid after the start of the turn, so that the Ring's start-of-turn heal is not asked about.  The Ring is
		// no weapon, and the bow is borne by another companion.
		scn.AttachCardsTo(aragorn, barahir);
		scn.AttachCardsTo(boromir, gondorbow);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(anduril));

		// Boromir is no bearer for it, so Aragorn is the one bearer and nothing is asked.
		scn.FreepsPlayCard(anduril);

		assertAttachedTo(anduril, aragorn);
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertAttachedTo(barahir, aragorn);
		assertAttachedTo(gondorbow, boromir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAnAragornAndGoesOnTheRohanAragorn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var rohanaragorn = scn.GetFreepsCard("rohanaragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(anduril);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(anduril));

		scn.MoveCompanionsToTable(rohanaragorn);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetKeywordCount(rohanaragorn, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(anduril));
		scn.FreepsPlayCard(anduril);

		assertAttachedTo(anduril, rohanaragorn);
		assertEquals(1, scn.GetKeywordCount(rohanaragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AragornCannotTakeUpAnotherWeaponOnceHeBearsItButMayBearWhatIsNoWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var knight = scn.GetFreepsCard("knight");
		var bow = scn.GetFreepsCard("bow");
		var gondorbow = scn.GetFreepsCard("gondorbow");
		var barahir = scn.GetFreepsCard("barahir");
		scn.MoveCompanionsToTable(aragorn, boromir, knight);
		scn.MoveCardsToHand(anduril, bow, gondorbow, barahir);

		scn.StartGame();

		// Without Anduril, Aragorn may take up his bow.
		assertTrue(scn.FreepsPlayAvailable(bow));
		scn.FreepsPlayCard(anduril);
		assertAttachedTo(anduril, aragorn);

		assertFalse(scn.FreepsPlayAvailable(bow));
		assertInZone(Zone.HAND, bow);

		// Another weapon goes to either of the other [gondor] Men, never to Aragorn.
		assertTrue(scn.FreepsPlayAvailable(gondorbow));
		scn.FreepsPlayCard(gondorbow);
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, knight));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(knight);
		assertAttachedTo(gondorbow, knight);

		// An artifact that is no weapon he may still bear.
		assertTrue(scn.FreepsPlayAvailable(barahir));
		scn.FreepsPlayCard(barahir);
		assertAttachedTo(barahir, aragorn);
		assertAttachedTo(anduril, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionAtSite2PlaysHisOwnSite3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);

		var site3 = scn.GetFreepsSite(3);
		var shadowSite3 = scn.GetShadowSite(3);

		scn.StartGame();
		scn.SkipToSite(2);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, site3, shadowSite3);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(anduril));

		scn.FreepsUseCardAction(anduril);

		assertSame(site3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_PATH, site3);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite3);
		assertNull(scn.GetSite(4));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileHisOwnNextSiteAlreadyLies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);

		var site3 = scn.GetFreepsSite(3);

		scn.StartGame();
		scn.SkipToSite(2);
		// His own Rivendell Valley lies at site 3, as it does where he has played his next site already; on a path
		// of numbered sites he has no other site 3 to play.
		scn.MoveCardToAdventurePath(site3);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertSame(site3, scn.GetSite(3));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(anduril));
	}

	@Test
	public void RegroupActionAtSite5PlaysHisOwnSite6() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);

		var site6 = scn.GetFreepsSite(6);
		var shadowSite6 = scn.GetShadowSite(6);

		scn.StartGame();
		scn.SkipToSite(4);
		// No minion is played, so the turn goes from the move to site 5 straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(6));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(anduril));

		scn.FreepsUseCardAction(anduril);

		assertSame(site6, scn.GetSite(6));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite6);
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ReplacesTheOpponentsSite3WithHisOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);

		var site3 = scn.GetFreepsSite(3);
		var shadowSite3 = scn.GetShadowSite(3);

		scn.StartGame();
		scn.SkipToSite(2);
		// The other player's Rivendell Valley lies at site 3, as it does where he has played his next site himself.
		// Both players hold the same nine sites here, so the site that replaces it is its twin.
		scn.MoveCardToAdventurePath(shadowSite3);
		scn.FreepsRefreshDecision();

		assertSame(shadowSite3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, site3);
		assertTrue(scn.FreepsActionAvailable(anduril));

		scn.FreepsUseCardAction(anduril);

		assertSame(site3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_PATH, site3);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite3);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ActionIsNotOfferedAtSites1Or3Or4Or6() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(anduril));

		scn.SkipToSite(3);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(4));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(anduril));

		scn.SkipToSite(4);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(5));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(anduril));

		scn.SkipToSite(6);
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(7));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(anduril));
	}
}
