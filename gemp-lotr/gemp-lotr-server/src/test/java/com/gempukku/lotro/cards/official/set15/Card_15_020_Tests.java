package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_020_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blessing", "15_20");

					put("uruviel", "15_26");    // Uruviel, Woodland Maid: [elven] follower, Aid (1); a trigger on her transfer only
					put("woodhall", "18_17");   // Woodhall Elf, Exile: [elven] follower, Aid (2); a trigger on her transfer only, so cheated on
					put("lorienelf", "1_53");   // Lorien Elf: [elven] Elf companion; its only text is a requirement to play
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: Elf archer companion; +3 only against a Nazgul
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: [elven] Elf ally; a fellowship ability only
					put("army", "18_47");       // Elendil's Army: [gondor] follower; its Aid exerts a [gondor] companion, and none is here

					put("marksman1", "1_176");  // Goblin Marksman: a [moria] archer minion and nothing else
					put("marksman2", "1_176");
					put("marksman3", "1_176");
					put("marksman4", "1_176");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LoriensBlessingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Lórien's Blessing
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Spot an [elven] follower to make the minion archery total -3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blessing");

		assertEquals("Lórien's Blessing", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAnElvenFollowerInTheSupportAreaMakesTheMinionArcheryTotalMinusThreeForTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blessing = scn.GetFreepsCard("blessing");
		var uruviel = scn.GetFreepsCard("uruviel");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(lorienelf);
		scn.MoveCardsToSupportArea(uruviel);
		scn.MoveCardsToHand(blessing);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("marksman1"), scn.GetShadowCard("marksman2"),
				scn.GetShadowCard("marksman3"), scn.GetShadowCard("marksman4"));
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertInZone(Zone.SUPPORT, uruviel);
		// Four archer minions.
		assertEquals(4, scn.GetShadowArcheryTotal());
		assertTrue(scn.FreepsPlayAvailable(blessing));

		scn.FreepsPlayCard(blessing);
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertInDiscard(blessing);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// In force as the fire is placed: one wound on the companions, not four. Gone with the phase: the
		// assignment phase reads the four archers' 4 again.
		assertEquals(0, scn.GetWoundsOn(lorienelf));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.PassArcheryActions();
		scn.FreepsChooseCard(lorienelf);
		assertEquals(1, scn.GetWoundsOn(lorienelf));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(4, scn.GetShadowArcheryTotal());
	}

	@Test
	public void ABorneElvenFollowerOfAnotherTitleIsSpottedAndTheFellowshipArcheryTotalIsUnchanged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blessing = scn.GetFreepsCard("blessing");
		var woodhall = scn.GetFreepsCard("woodhall");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(lorienelf, legolas);
		scn.AttachCardsTo(lorienelf, woodhall);
		scn.MoveCardsToHand(blessing);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("marksman1"), scn.GetShadowCard("marksman2"),
				scn.GetShadowCard("marksman3"), scn.GetShadowCard("marksman4"));
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertAttachedTo(woodhall, lorienelf);
		// Legolas, an archer, adds 1 to the fellowship archery total.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(4, scn.GetShadowArcheryTotal());
		assertTrue(scn.FreepsPlayAvailable(blessing));

		scn.FreepsPlayCard(blessing);
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertInDiscard(blessing);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void CannotBePlayedWithAnElvenCardThatIsNoFollowerOrAFollowerOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blessing = scn.GetFreepsCard("blessing");
		var celeborn = scn.GetFreepsCard("celeborn");
		var army = scn.GetFreepsCard("army");
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(army);
		scn.MoveCardsToHand(blessing);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("marksman1"));
		scn.SkipToPhase(Phase.ARCHERY);

		assertInZone(Zone.SUPPORT, celeborn, army);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertFalse(scn.FreepsPlayAvailable(blessing));
		assertInHand(blessing);
	}
}
