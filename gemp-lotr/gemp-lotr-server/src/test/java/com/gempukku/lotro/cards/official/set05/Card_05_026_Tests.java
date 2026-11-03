package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_026_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("look", "5_26");
					// Sméagol, Poor Creature: [gollum] companion; a skirmish ability used only on purpose
					put("smeagol", "6_45");
					// Smeagol, Once Like Us: the [shire] Sméagol; an assignment ability used only on purpose
					put("shiresmeagol", "103_116");
					// Gollum, Stinker: [gollum] minion; strength +1 for each burden, and a trigger on his own win
					put("gollum", "5_25");
					// Gollum, Half a Wraith Himself: the [wraith] Gollum; a trigger on playing a Nazgûl, and an assignment ability
					put("wraithgollum", "103_66");
					// Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum; damage +1 only in a fierce skirmish
					put("shelob", "8_26");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LookatHimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Look at Him
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Shadow:</b> Spot Sméagol or Gollum to add (1) for each burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("look");

		assertEquals("Look at Him", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingTheFreePeoplesPlayersSmeagolAddsOneForEachBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var look = scn.GetShadowCard("look");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(look);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(look));

		scn.ShadowPlayCard(look);
		// Twilight cost 0, and (1) for each of the 2 burdens.
		assertEquals(pool + 2, scn.GetTwilight());
		assertEquals(2, scn.GetBurdens());
		assertInDiscard(look);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingTheShireSmeagolAddsOneForEachBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var look = scn.GetShadowCard("look");
		var shiresmeagol = scn.GetFreepsCard("shiresmeagol");
		scn.MoveCompanionsToTable(shiresmeagol);
		scn.MoveCardsToHand(look);

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(look));

		scn.ShadowPlayCard(look);
		// Twilight cost 0, and (1) for each of the 3 burdens.
		assertEquals(pool + 3, scn.GetTwilight());
		assertInDiscard(look);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingTheShadowPlayersGollumAddsOneForEachBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var look = scn.GetShadowCard("look");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(look);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(look));

		scn.ShadowPlayCard(look);
		// Twilight cost 0, and (1) for the one burden.
		assertEquals(pool + 1, scn.GetTwilight());
		assertInDiscard(look);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingTheWraithGollumAddsOneForEachBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var look = scn.GetShadowCard("look");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		scn.MoveCardsToHand(look);

		scn.StartGame();
		scn.MoveMinionsToTable(wraithgollum);
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(look));

		scn.ShadowPlayCard(look);
		assertEquals(pool + 2, scn.GetTwilight());
		assertInDiscard(look);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNeitherSmeagolNorGollumInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var look = scn.GetShadowCard("look");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToHand(look);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.HAND, look);
		scn.FreepsPass();

		// Shelob is a [gollum] minion, but no Gollum.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.HAND, look);
		assertFalse(scn.ShadowPlayAvailable(look));
	}
}
