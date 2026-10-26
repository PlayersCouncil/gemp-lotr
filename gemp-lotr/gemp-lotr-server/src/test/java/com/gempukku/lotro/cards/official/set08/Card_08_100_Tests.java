package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_100_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("servitor", "8_100");
					put("servitor2", "8_100");
					put("legolas", "1_50");     // Legolas, Greenleaf: an archer with an archery special ability
					put("garrison", "8_36");    // Garrison of Gondor: an archer; his other text needs a fortification
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("bow", "1_90");         // Aragorn's Bow: makes Aragorn an archer; an archery special ability of a possession
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf to bear the Bracers; a skirmish ability used only on purpose
					put("bracers", "2_3");      // Dwarven Bracers: a Dwarf's possession; Response: discard it to prevent a wound to its bearer
					put("marksman", "1_176");   // Goblin Marksman: an archer minion, vitality 1, no other text
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothServitorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Gorgoroth Servitor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>.<br><b>Archery:</b> Return this minion to your hand to make the fellowship archery total -2 and prevent the Free Peoples player from using archery special abilities.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("servitor");

		assertEquals("Gorgoroth Servitor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ReturnedToHandItMakesTheFellowshipArcheryTotalMinusTwoAndBarsTheFreePeoplesArcheryAbilities() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var garrison = scn.GetFreepsCard("garrison");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(legolas, garrison, aragorn);
		scn.AttachCardsTo(aragorn, bow);

		var servitor = scn.GetShadowCard("servitor");
		var servitor2 = scn.GetShadowCard("servitor2");

		scn.StartGame();
		scn.MoveMinionsToTable(servitor, servitor2);
		scn.SkipToPhase(Phase.ARCHERY);

		// Three archers: Legolas, the Garrison and Aragorn with his Bow.  Before the ability, the archery special
		// abilities of a companion and of a possession are offered to the Free Peoples player.
		assertEquals(3, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(legolas));
		assertTrue(scn.FreepsActionAvailable(bow));
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(servitor));
		scn.ShadowUseCardAction(servitor);

		// 3 - 2: a -1 would read 2, a -3 or a total set to 0 would read 0.  The Servitor used is in its owner's
		// hand; the other is still in play.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertInZone(Zone.HAND, servitor);
		assertInZone(Zone.SHADOW_CHARACTERS, servitor2);

		// The Free Peoples player's archery special abilities are barred.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(legolas));
		assertFalse(scn.FreepsActionAvailable(bow));
		scn.FreepsPass();

		// The Shadow player's are not.
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.ShadowActionAvailable(servitor2));
		scn.ShadowPass();

		// Archery fire uses the lowered total: one wound, placed on the one minion, and then the assignment phase,
		// where the total is back at 3.
		assertEquals(1, scn.GetWoundsOn(servitor2));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(3, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void TheBarOnArcherySpecialAbilitiesLastsOnlyForTheArcheryPhaseItWasUsedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		var servitor = scn.GetShadowCard("servitor");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(servitor, savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.FreepsActionAvailable(legolas));
		scn.FreepsPass();
		scn.ShadowUseCardAction(servitor);
		assertInZone(Zone.HAND, servitor);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(legolas));

		// A second archery phase in the same turn, after a second move: Legolas's ability is back.
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(savage2);
		scn.ShadowRefreshDecision();
		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(legolas));
	}

	@Test
	public void AResponseAbilityIsNoArcherySpecialAbilityAndIsStillUsableUnderTheBar() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var bracers = scn.GetFreepsCard("bracers");
		scn.MoveCompanionsToTable(legolas, gimli);
		scn.AttachCardsTo(gimli, bracers);

		var servitor = scn.GetShadowCard("servitor");
		var marksman = scn.GetShadowCard("marksman");

		assertAttachedTo(bracers, gimli);
		scn.StartGame();
		scn.MoveMinionsToTable(servitor, marksman);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.FreepsActionAvailable(legolas));
		scn.FreepsPass();
		scn.ShadowUseCardAction(servitor);
		// 1 - 2, to no lower than 0.
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(legolas));
		scn.PassArcheryActions();

		// The Marksman's archery wound, placed on Gimli, is answered by his Bracers.
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsChooseCard(gimli);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bracers));
		scn.FreepsAcceptOptionalTrigger();
		assertInZone(Zone.DISCARD, bracers);
		assertEquals(0, scn.GetWoundsOn(gimli));
		// The fellowship's total of 0 put no wound on the Marksman.
		assertEquals(0, scn.GetWoundsOn(marksman));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsOfferedInTheArcheryPhaseAndNotInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servitor = scn.GetShadowCard("servitor");

		scn.StartGame();
		scn.MoveMinionsToTable(servitor);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(servitor));
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.ShadowActionAvailable(servitor));
	}
}
