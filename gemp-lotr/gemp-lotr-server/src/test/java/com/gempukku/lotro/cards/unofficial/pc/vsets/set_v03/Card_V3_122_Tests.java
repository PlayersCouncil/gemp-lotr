package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_122_Tests
{
	// The Ring-bearer is Frodo, Son of Drogo. Every card named here is in both players' decks.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Shadow events, by the phase each is for. None of them is ever played.
					put("power", "1_136");       // Saruman's Power: [isengard], Shadow
					put("forces", "1_248");      // Forces of Mordor: [sauron], Shadow
					put("hate", "1_250");        // Hate: [sauron], Maneuver
					put("hunt", "1_126");        // Hunt Them Down!: [isengard], Maneuver
					put("flanking", "4_234");    // Flanking Attack: [raider], "Maneuver or Skirmish"
					put("parry", "1_132");       // Parry: [isengard], Maneuver
					put("terror", "1_226");      // Their Power Is in Terror: [ringwraith], Maneuver
					put("malice", "3_79");       // Malice: [moria], Archery
					put("frenzy", "1_171");      // Frenzy: [moria], Assignment
					put("savagery", "1_139");    // Savagery to Match Their Numbers: [isengard], Skirmish
					put("drums", "1_168");       // Drums in the Deep: [moria], Skirmish
					put("gleaming", "3_89");     // Gleaming in the Snow: [sauron], Regroup
					put("blades", "1_203");      // All Blades Perish: [ringwraith], Response (for no phase)

					// Free Peoples events
					put("sentinels", "2_20");    // Secret Sentinels: [elven], Maneuver
					put("delving", "1_6");       // Delving: [dwarven], Fellowship
					put("intimidate", "1_76");   // Intimidate: [gandalf], Response

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8
					put("gimli", "1_13");        // Gimli, Son of Gloin: strength 6
					put("runner1", "1_178");     // Goblin Runner: strength 5, vitality 1; cheated in, so its text does nothing
					put("runner2", "1_178");
				}},
				new HashMap<>()
				{{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "103_122");     // Desolation of Ash
					put("site9", "7_360");       // Dagorlad: adds 3 threats when the fellowship moves there
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesolationOfAshStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Desolation of Ash
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 8
		 * Shadow Number: 8
		 * Direction: Left
		 * Game Text: At the start of each phase, the Shadow player may remove (1) to take a Shadow event for that
		 * 	phase into hand from discard.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Desolation of Ash", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void AtTheStartOfTheManeuverPhaseTheShadowPlayerRemovesOneToTakeAShadowManeuverEventFromHisDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hate = scn.GetShadowCard("hate");
		var hunt = scn.GetShadowCard("hunt");
		var flanking = scn.GetShadowCard("flanking");
		var savagery = scn.GetShadowCard("savagery");
		var blades = scn.GetShadowCard("blades");
		var sentinels = scn.GetShadowCard("sentinels");
		var parry = scn.GetShadowCard("parry");
		var terror = scn.GetShadowCard("terror");
		var freepsParry = scn.GetFreepsCard("parry");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));
		// A minion, so that there is a maneuver phase.
		scn.MoveMinionsToTable(scn.GetShadowCard("runner1"));
		// In the Shadow player's discard pile: three Shadow events for the maneuver phase, of three cultures
		// (one of them for the skirmish phase as well), and beside them a Shadow event for the skirmish phase, a
		// Shadow event for no phase, and his own copy of a Free Peoples event for the maneuver phase.
		scn.MoveCardsToDiscard(hate, hunt, flanking, savagery, blades, sentinels);
		// Shadow events for the maneuver phase that are not in his discard pile: in his hand, on his draw
		// deck, and (the other player's copy of the one in his hand) in the other player's discard pile.
		scn.MoveCardsToHand(parry);
		scn.MoveCardsToTopOfDeck(terror);
		scn.MoveCardsToDiscard(freepsParry);

		scn.FreepsPass();
		var site = scn.GetCurrentSite();
		assertEquals("Desolation of Ash", site.getBlueprint().getTitle());
		scn.SkipToPhase(Phase.MANEUVER);

		// 2 companions + 8 for the site
		assertEquals(10, scn.GetTwilight());
		int shadowHand = scn.GetShadowHandCount();
		int shadowDiscard = scn.GetShadowDiscardCount();
		int freepsHand = scn.GetFreepsHandCount();
		int freepsDiscard = scn.GetFreepsDiscardCount();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals("false", scn.ShadowActionNoEffect(site));

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(hate, hunt, flanking));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savagery, blades, sentinels, parry, terror));
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCardBPFromSelection(hunt);
		assertInZone(Zone.HAND, hunt);
		assertEquals(shadowHand + 1, scn.GetShadowHandCount());
		assertEquals(shadowDiscard - 1, scn.GetShadowDiscardCount());
		assertInDiscard(hate, flanking, savagery, blades, sentinels);
		assertInZone(Zone.HAND, parry);
		assertInZone(Zone.DECK, terror);
		assertInDiscard(freepsParry);
		assertEquals(freepsHand, scn.GetFreepsHandCount());
		assertEquals(freepsDiscard, scn.GetFreepsDiscardCount());
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DecliningLeavesThePoolAndTheDiscardPileAsTheyWereAndTheOfferIsNotMadeAgainInThatPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hate = scn.GetShadowCard("hate");
		var hunt = scn.GetShadowCard("hunt");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));
		scn.MoveMinionsToTable(scn.GetShadowCard("runner1"));
		scn.MoveCardsToDiscard(hate, hunt);

		scn.FreepsPass();
		var site = scn.GetCurrentSite();
		scn.SkipToPhase(Phase.MANEUVER);

		// 2 companions + 8 for the site
		assertEquals(10, scn.GetTwilight());
		int shadowHand = scn.GetShadowHandCount();
		int shadowDiscard = scn.GetShadowDiscardCount();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));

		scn.ShadowDeclineOptionalTrigger();
		assertEquals(10, scn.GetTwilight());
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertEquals(shadowDiscard, scn.GetShadowDiscardCount());
		assertInDiscard(hate, hunt);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The phase goes on: the offer was for its start, and is not made again when the Shadow player's
		// turn to act in it comes.
		scn.FreepsPass();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertInDiscard(hate, hunt);
		assertEquals(10, scn.GetTwilight());
	}

	@Test
	public void IsNotOfferedWhileThePoolIsEmptyAndIsOfferedWithExactlyOneToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var hate = scn.GetShadowCard("hate");
		var hunt = scn.GetShadowCard("hunt");
		var malice = scn.GetShadowCard("malice");
		var frenzy = scn.GetShadowCard("frenzy");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));
		scn.MoveMinionsToTable(scn.GetShadowCard("runner1"));
		// An event for each of four phases in a row.
		scn.MoveCardsToDiscard(power, hate, hunt, malice, frenzy);

		scn.FreepsPass();
		var site = scn.GetCurrentSite();

		// The Shadow phase, with 2 companions + 8 for the site in the pool: offered, and declined.
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowDeclineOptionalTrigger();
		assertInDiscard(power);

		// The maneuver phase starts with nothing in the pool: no offer, though two events for it are there.
		scn.SetTwilight(0);
		scn.ShadowPass();
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInDiscard(hate, hunt);

		// The archery phase starts with exactly (1): offered, and taking it empties the pool.
		scn.SetTwilight(1);
		scn.PassManeuverActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.HAND, malice);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// The assignment phase starts with the pool empty again: no offer.
		scn.PassArcheryActions();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInDiscard(frenzy);
	}

	@Test
	public void TheOfferIsMadeAtTheStartOfEveryPhaseForThatPhasesEventsAndNotOnceTheFellowshipHasMovedOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		var power = scn.GetShadowCard("power");
		var forces = scn.GetShadowCard("forces");
		var hate = scn.GetShadowCard("hate");
		var flanking = scn.GetShadowCard("flanking");
		var malice = scn.GetShadowCard("malice");
		var frenzy = scn.GetShadowCard("frenzy");
		var savagery = scn.GetShadowCard("savagery");
		var drums = scn.GetShadowCard("drums");
		var gleaming = scn.GetShadowCard("gleaming");
		var blades = scn.GetShadowCard("blades");
		var sentinels = scn.GetShadowCard("sentinels");
		var delving = scn.GetShadowCard("delving");
		var intimidate = scn.GetShadowCard("intimidate");

		scn.StartGame();
		scn.SkipToSite(7);
		// The Shadow player's hand goes back on his deck, so that the events he takes are all he holds and
		// he has nothing to discard when he reconciles.
		scn.MoveCardsToBottomOfDeck(scn.GetShadowHand().toArray(new PhysicalCardImpl[0]));
		assertEquals(0, scn.GetShadowHandCount());
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.MoveCardsToDiscard(power, forces, hate, flanking, malice, frenzy, savagery, drums, gleaming);
		// Never to be taken: a Shadow event for no phase, and the Shadow player's own Free Peoples events.
		scn.MoveCardsToDiscard(blades, sentinels, delving, intimidate);

		scn.FreepsPass();
		var site = scn.GetCurrentSite();
		assertEquals("Desolation of Ash", site.getBlueprint().getTitle());
		// 3 companions + 8 for the site
		assertEquals(11, scn.GetTwilight());

		// The Shadow phase: the two Shadow-phase events.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(power, forces));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(power);
		assertInZone(Zone.HAND, power);
		assertInDiscard(forces);
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPass();

		// The maneuver phase: the maneuver event, and the event for the maneuver or the skirmish phase.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(hate, flanking));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(hate);
		assertInZone(Zone.HAND, hate);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.PassManeuverActions();

		// The archery phase: one event for it, which is taken without a choice.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.HAND, malice);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		scn.PassArcheryActions();

		// The assignment phase: likewise.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.HAND, frenzy);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner1}, new PhysicalCardImpl[]{gimli, runner2});

		// The first skirmish: the two skirmish events, and the event for the maneuver or the skirmish phase.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(savagery, drums, flanking));
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(flanking);
		assertInZone(Zone.HAND, flanking);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// The second skirmish is a skirmish phase of its own: the offer is made again.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(savagery, drums));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(drums);
		assertInZone(Zone.HAND, drums);
		assertInDiscard(savagery);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// The regroup phase: one event for it.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.HAND, gleaming);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// Seven phases, seven events, (7) removed; what was for none of those phases is where it was.
		assertEquals(7, scn.GetShadowHandCount());
		assertInDiscard(forces, savagery, blades, sentinels, delving, intimidate);

		// The fellowship moves on to site 9. A Shadow-phase event is still in the discard pile and the pool
		// is not empty, but this site's text is behind the fellowship: no offer.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertTrue(scn.GetTwilight() > 0);
		assertInDiscard(forces);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhoeverIsTheShadowPlayerOfTheTurnIsTheOneAskedAndTakesFromHisOwnDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var firstPower = scn.GetFreepsCard("power");
		var firstForces = scn.GetFreepsCard("forces");
		var firstGleaming = scn.GetFreepsCard("gleaming");
		var secondPower = scn.GetShadowCard("power");
		var secondForces = scn.GetShadowCard("forces");
		var secondGleaming = scn.GetShadowCard("gleaming");

		scn.StartGame();
		scn.SkipToSite(7);
		// Each player has the same events in his own discard pile: two for the Shadow phase, one for the
		// regroup phase.
		scn.MoveCardsToDiscard(firstPower, firstForces, firstGleaming);
		scn.MoveCardsToDiscard(secondPower, secondForces, secondGleaming);

		// The first player's Frodo goes on to site 8 alone. The second player is the Shadow player: he is
		// asked at the start of the Shadow phase and, there being no minion to fight, of the regroup phase.
		scn.FreepsPass();
		var site = scn.GetCurrentSite();
		assertEquals("Desolation of Ash", site.getBlueprint().getTitle());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();
		scn.ShadowPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SkipToOtherPlayersTurn();

		// The second player's fellowship was still at site 7, and now moves to site 8 in its turn. The first
		// player is the Shadow player.
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertInDiscard(firstPower, firstForces, secondPower, secondForces);
		scn.ShadowPass();
		assertEquals(8, scn.GetCurrentSiteNumber());
		// 1 companion + 8 for the site
		assertEquals(9, scn.GetTwilight());
		int firstHand = scn.GetFreepsHandCount();
		int secondHand = scn.GetShadowHandCount();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(site));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(8, scn.GetTwilight());
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCardBPFromSelection(firstForces);
		assertInZone(Zone.HAND, firstForces);
		assertInDiscard(firstPower, secondPower, secondForces);
		assertEquals(firstHand + 1, scn.GetFreepsHandCount());
		assertEquals(secondHand, scn.GetShadowHandCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	// Ketura, 2026-10-07, on Q-0107: this card keeps its guard. Where nothing could be taken the Shadow
	// player is not asked, though the price could be paid: the site would otherwise ask at every phase.
	@Test
	public void WithNoShadowEventForThatPhaseInTheDiscardPileNoOfferIsMadeThoughOneCouldBeRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savagery = scn.GetShadowCard("savagery");
		var blades = scn.GetShadowCard("blades");
		var sentinels = scn.GetShadowCard("sentinels");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));
		scn.MoveMinionsToTable(scn.GetShadowCard("runner1"));
		// A Shadow event for the skirmish phase, a Shadow event for no phase, and the Shadow player's own
		// Free Peoples event for the maneuver phase: nothing the site could take at the start of the maneuver
		// phase.
		scn.MoveCardsToDiscard(savagery, blades, sentinels);

		scn.FreepsPass();
		scn.SkipToPhase(Phase.MANEUVER);

		// 2 companions + 8 for the site
		assertEquals(10, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(savagery, blades, sentinels);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
