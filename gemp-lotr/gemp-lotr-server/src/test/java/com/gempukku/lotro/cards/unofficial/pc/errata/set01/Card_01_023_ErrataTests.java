package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_023_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nobody", "51_23");

					put("gimli", "1_13");         // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3, damage +1
					put("grimir", "1_17");        // Grimir, Dwarven Elder: Dwarf ally, home site 3, strength 3
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, strength 8
					put("armor", "1_8");          // Dwarven Armor: Free Peoples possession borne by a Dwarf
					put("kingdom", "1_16");       // Greatest Kingdom of My People: Free Peoples condition in the support area;
					                              // it answers an Orc being played, and none is played here

					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3
					put("soldier", "1_271");      // Orc Soldier: strength 7, vitality 2
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("runner", "1_178");       // Goblin Runner: strength 5, vitality 1
					put("greed", "1_125");        // Greed: Shadow condition in the support area; its ability needs 6 companions
					put("greed2", "1_125");
					put("scimitar", "1_180");     // Goblin Scimitar: Shadow possession borne by a [moria] Orc, strength +2
					put("poison", "8_28");        // Spider Poison: Shadow possession a companion bears; nothing unless he is assigned
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A table whose Shadow draw deck is two cards once both minions are down: the Shadow player's own copies
	// of the event and of Gimli.
	protected VirtualTableScenario GetTwoCardDeckScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nobody", "51_23");
					put("gimli", "1_13");         // Gimli, Son of Gloin: strength 6, damage +1
					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3
					put("troop", "1_177");        // Goblin Patrol Troop: no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NobodyTossesaDwarfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Nobody Tosses a Dwarf
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If a Dwarf wins a skirmish, discard a Shadow card.  Its owner may discard 3 cards from the
		 * bottom of their draw deck to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nobody");

		assertEquals("Nobody Tosses a Dwarf", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ADwarfWinningASkirmishLetsAShadowCardOfAnyKindBeDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nobody = scn.GetFreepsCard("nobody");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var kingdom = scn.GetFreepsCard("kingdom");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var greed = scn.GetShadowCard("greed");
		var scimitar = scn.GetShadowCard("scimitar");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToSupportArea(kingdom);
		scn.MoveCardsToHand(nobody);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.MoveCardsToSupportArea(greed);
		scn.AttachCardsTo(troop, scimitar);
		scn.AttachCardsTo(aragorn, poison);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, savage);

		var pool = scn.GetTwilight();
		var shadowDeck = scn.GetShadowDeckCount();
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		// 6 against 5: Gimli wins, and the Savage survives his two wounds.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(nobody));

		scn.FreepsChooseOptionalTrigger(nobody);
		// The minion that lost, a minion that did not fight, a condition in the support area, a possession on
		// a minion and a possession on a companion: Shadow cards all.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop, greed, scimitar, poison));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, aragorn, armor, kingdom, scn.GetRingBearer(), scn.GetCurrentSite()));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(poison);
		assertTrue(scn.ShadowDecisionAvailable("discard the bottom 3 cards"));
		assertAttachedTo(poison, aragorn);

		scn.ShadowChooseNo();
		assertInZone(Zone.DISCARD, poison);
		assertInZone(Zone.DISCARD, nobody);
		assertInZone(Zone.SHADOW_CHARACTERS, savage, troop);
		assertInZone(Zone.SUPPORT, greed);
		assertAttachedTo(scimitar, troop);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AHinderedShadowCardMayBeTheOneDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nobody = scn.GetFreepsCard("nobody");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		var hindered = scn.GetShadowCard("greed");
		var unhindered = scn.GetShadowCard("greed2");
		scn.MoveCardsToHand(nobody);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(hindered, unhindered);
		// A hindered card has no type or culture, and is a Shadow card still.
		scn.HinderCard(hindered);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, savage);
		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.IsHindered(hindered));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(nobody));
		scn.FreepsChooseOptionalTrigger(nobody);

		assertTrue(scn.FreepsHasCardChoiceAvailable(hindered, unhindered, savage));
		scn.FreepsChooseCard(hindered);
		assertTrue(scn.ShadowDecisionAvailable("discard the bottom 3 cards"));
		scn.ShadowChooseNo();

		assertInZone(Zone.DISCARD, hindered);
		assertInZone(Zone.SUPPORT, unhindered);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOwnerMayDiscardTheBottom3CardsOfHisDrawDeckToPreventTheDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nobody = scn.GetFreepsCard("nobody");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(nobody);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, savage);
		scn.FreepsPass();
		scn.ShadowPass();

		var bottom1 = scn.GetFromBottomOfShadowDeck(1);
		var bottom2 = scn.GetFromBottomOfShadowDeck(2);
		var bottom3 = scn.GetFromBottomOfShadowDeck(3);
		var bottom4 = scn.GetFromBottomOfShadowDeck(4);
		var top = scn.GetShadowTopOfDeck();
		var shadowDeck = scn.GetShadowDeckCount();
		var freepsDeck = scn.GetFreepsDeckCount();
		assertTrue(shadowDeck > 4);
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.FreepsChooseOptionalTrigger(nobody);
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		scn.FreepsChooseCard(troop);
		assertTrue(scn.ShadowDecisionAvailable("discard the bottom 3 cards"));
		assertInZone(Zone.DECK, bottom1, bottom2, bottom3);

		scn.ShadowChooseYes();
		assertInZone(Zone.DISCARD, bottom1, bottom2, bottom3);
		assertInZone(Zone.DECK, bottom4, top);
		assertEquals(shadowDeck - 3, scn.GetShadowDeckCount());
		assertEquals(3, scn.GetShadowDiscardCount());
		// The Troop is still in play; the event is spent all the same, and the other deck did not pay.
		assertInZone(Zone.SHADOW_CHARACTERS, troop, savage);
		assertInZone(Zone.DISCARD, nobody);
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOwnerIsNotAskedWithTwoCardsInHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoCardDeckScenario();

		var nobody = scn.GetFreepsCard("nobody");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(nobody);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		assertEquals(2, scn.GetShadowDeckCount());

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, savage);
		scn.FreepsPass();
		scn.ShadowPass();

		scn.FreepsChooseOptionalTrigger(nobody);
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		scn.FreepsChooseCard(troop);

		assertFalse(scn.ShadowDecisionAvailable("discard the bottom 3 cards"));
		assertInZone(Zone.DISCARD, troop);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(2, scn.GetShadowDeckCount());
		// The Troop and nothing else
		assertEquals(1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenACompanionWhoIsNoDwarfWinsOrWhenADwarfLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nobody = scn.GetFreepsCard("nobody");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(nobody);
		scn.MoveCompanionsToTable(gimli, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, soldier);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{gimli, soldier});

		// 8 against 5: Aragorn, a Man, wins beside a Dwarf.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(nobody));

		// 6 against 7: Gimli loses.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(nobody));
		assertInZone(Zone.HAND, nobody);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADwarfAllyWinningASkirmishAtHisHomeSiteIsADwarfWinning() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nobody = scn.GetFreepsCard("nobody");
		var grimir = scn.GetFreepsCard("grimir");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(nobody);
		// 3 + 3 against the Savage's 5
		scn.ApplyAdHocModifier(new StrengthModifier(null, grimir, null, 3));
		scn.MoveMinionsToTable(savage, troop);

		// The fellowship moves to site 3, Grimir's home.
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(grimir, savage);
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(nobody));

		scn.FreepsChooseOptionalTrigger(nobody);
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		scn.FreepsChooseCard(savage);
		scn.ShadowChooseNo();
		assertInZone(Zone.DISCARD, savage);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.DISCARD, nobody);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsOfferedThoughTheWinKilledTheOnlyShadowCardInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nobody = scn.GetFreepsCard("nobody");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(nobody);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);
		scn.FreepsPass();
		scn.ShadowPass();

		// The Runner, vitality 1, is dead; an event may be played whatever its effect will find to do.
		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.HAND, nobody);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(nobody));
	}

	@Test
	public void WithNoShadowCardLeftInPlayTheEventIsSpentAndNobodyIsAskedAnything() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nobody = scn.GetFreepsCard("nobody");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(nobody);
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);
		scn.FreepsPass();
		scn.ShadowPass();

		var shadowDeck = scn.GetShadowDeckCount();
		assertInZone(Zone.DISCARD, runner);

		scn.FreepsChooseOptionalTrigger(nobody);
		assertFalse(scn.ShadowDecisionAvailable("discard the bottom 3 cards"));
		assertInZone(Zone.DISCARD, nobody);
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
