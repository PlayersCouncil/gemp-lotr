package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_076_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("helpless", "2_76");

					// Sam, Son of Hamfast: Fellowship: exert Sam to remove a burden; Response: if Frodo dies, make Sam
					// the Ring-bearer.  Ring-bound by rule.
					put("sam", "1_311");
					// Merry, From O'er the Brandywine: a [shire] companion who is no Sam and no Frodo; strength +2
					// only while he bears a weapon
					put("merry", "1_303");
					// Merry, Friend to Sam: a companion with the Frodo signet, for Frodo's own Fellowship ability;
					// his skirmish ability is never used here
					put("merry2", "1_302");

					// Úlairë Nelya, Ringwraith in Twilight: a [wraith] Nazgûl, site 2; her text needs 2 burdens or 2
					// wounds on the Ring-bearer
					put("nelya", "2_84");
					// Morgul Creeper: a [wraith] minion who is no Nazgûl (an Orc); his text needs 3 burdens
					put("creeper", "8_75");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli, Bearer of Grudges is the Ring-bearer, and no Frodo is in play.
	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("helpless", "2_76");
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] companion who is no Frodo
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a [shire] companion who is no Frodo
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: a [wraith] Nazgûl
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	// Sam, Bearer of Great Need is the Ring-bearer, so that Frodo, Frenzied Fighter can stand beside him as a Frodo
	// who is not the Ring-bearer ("This version of Frodo cannot become the Ring-bearer").
	protected VirtualTableScenario GetSamRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("helpless", "2_76");
					// Frodo, Frenzied Fighter: Ring-bound; cannot be overwhelmed unless tripled while minions
					// outnumber companions; his skirmish ability is never used here
					put("frodo", "13_149");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl; his text needs 3 burdens
					put("khamul", "103_61");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.SamRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HelplessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Helpless
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: To play, spot Frodo and a Nazgûl. Plays on Sam.<br>Sam's game text does not apply.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("helpless");

		assertEquals("Helpless", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedOnSamOnceANazgulIsPlayedBesideFrodo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var helpless = scn.GetShadowCard("helpless");
		var nelya = scn.GetShadowCard("nelya");
		var creeper = scn.GetShadowCard("creeper");
		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveCardsToHand(helpless, nelya);
		assertInHand(helpless, nelya);

		scn.StartGame();
		scn.MoveMinionsToTable(creeper);
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.SHADOW);
		assertTrue(scn.AwaitingShadowPhaseActions());
		// Frodo and a [wraith] minion who is no Nazgûl: not yet.
		assertFalse(scn.ShadowPlayAvailable(helpless));

		scn.ShadowPlayCard(nelya);
		assertTrue(scn.ShadowPlayAvailable(helpless));
		scn.ShadowPlayCard(helpless);

		// Sam is the only bearer it may have: neither Frodo nor Merry is offered, and it goes on Sam unasked.
		assertAttachedTo(helpless, sam);
		assertNotAttachedTo(helpless, frodo);
		assertNotAttachedTo(helpless, merry);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutFrodo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var helpless = scn.GetShadowCard("helpless");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCompanionsToTable(sam, merry);
		scn.MoveCardsToHand(helpless);
		assertInHand(helpless);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);

		scn.SkipToPhase(Phase.SHADOW);
		// A Nazgûl and Sam to bear it, but the Ring-bearer is Gimli, and Sam and Merry are [shire] companions who
		// are no Frodo.
		assertEquals("Gimli", scn.GetRingBearer().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(helpless));
		assertInHand(helpless);
	}

	@Test
	public void CannotBePlayedWithNoSamToBearIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var helpless = scn.GetShadowCard("helpless");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(helpless);
		assertInHand(helpless);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);

		scn.SkipToPhase(Phase.SHADOW);
		// Frodo and a Nazgûl can be spotted, but neither Frodo nor Merry may bear it.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(helpless));
		assertInHand(helpless);
	}

	@Test
	public void IsPlayedOnSamTheRingBearerBesideAFrodoWhoIsNotAndANazgulOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSamRingBearerScenario();

		var frodo = scn.GetFreepsCard("frodo");
		var helpless = scn.GetShadowCard("helpless");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCompanionsToTable(frodo);
		scn.MoveCardsToHand(helpless);
		assertInHand(helpless);

		scn.StartGame();
		var sam = scn.GetRingBearer();
		assertEquals("Sam", sam.getBlueprint().getTitle());
		scn.MoveMinionsToTable(khamul);

		scn.SkipToPhase(Phase.SHADOW);
		// Resistance 5, +1 for each Hobbit he can spot: himself and Frodo.
		assertEquals(7, scn.GetResistance(sam));
		assertTrue(scn.HasKeyword(sam, Keyword.RING_BOUND));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(helpless));

		scn.ShadowPlayCard(helpless);

		assertAttachedTo(helpless, sam);
		// His text no longer applies: back to his printed resistance.  As the Ring-bearer he is Ring-bound by
		// rule, whatever becomes of his text.
		assertEquals(5, scn.GetResistance(sam));
		assertTrue(scn.HasKeyword(sam, Keyword.RING_BOUND));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SamsGameTextDoesNotApplyWhileHeBearsItAndAnotherCompanionsStillDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry2 = scn.GetFreepsCard("merry2");
		var helpless = scn.GetShadowCard("helpless");
		scn.MoveCompanionsToTable(sam, merry2);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.AddWoundsToChar(frodo, 1);
		scn.FreepsRefreshDecision();

		// Without it: a burden for Sam to remove, a wound for Frodo to heal, and Sam is Ring-bound.
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(sam));
		assertTrue(scn.FreepsActionAvailable(frodo));
		assertTrue(scn.HasKeyword(sam, Keyword.RING_BOUND));

		// As if the Shadow player had played it on an earlier turn.
		scn.AttachCardsTo(sam, helpless);
		scn.FreepsRefreshDecision();
		assertAttachedTo(helpless, sam);

		// Sam's text is gone, his Ring-bound with it (he is not the Ring-bearer); Frodo's is not.
		assertFalse(scn.FreepsActionAvailable(sam));
		assertFalse(scn.HasKeyword(sam, Keyword.RING_BOUND));
		assertTrue(scn.FreepsActionAvailable(frodo));

		// Merry, the only other companion with the Frodo signet, exerts to heal Frodo.
		scn.FreepsUseCardAction(frodo);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry2));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
