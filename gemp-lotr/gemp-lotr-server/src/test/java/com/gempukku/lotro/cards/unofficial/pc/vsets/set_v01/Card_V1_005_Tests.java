package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_005_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("onedwarf", "101_5");
					put("onedwarf2", "101_5");

					// Gimli, Son of Gloin: Dwarf companion, vitality 3.  His skirmish ability is never used here.
					put("gimli", "1_13");
					put("guard", "1_7");         // Dwarf Guard: Dwarf companion, vitality 2, no text in play
					// Grimir, Dwarven Elder: Dwarf ally, vitality 3.  His fellowship ability is never used here.
					put("grimir", "1_17");
					// Aragorn, Ranger of the North: a Man, vitality 4.  His maneuver ability is never used here.
					put("aragorn", "1_89");

					// Items a Dwarf can bear.  Every test ends in the maneuver phase, and the only one of these that can
					// do anything before the archery phase or a skirmish is Dwarven Bracers, whose response is to its
					// bearer being about to take a wound, in any phase.  It never comes up: no bearer is wounded by
					// play in any test (the Dwarf is only healed, and the wounds he starts with are cheated on before
					// the game starts).
					put("axe", "1_9");           // Dwarven Axe: possession
					put("armor", "1_8");         // Dwarven Armor: possession
					put("bracers", "2_3");       // Dwarven Bracers: possession
					put("handaxe", "2_10");      // Hand Axe: possession
					put("ring", "9_9");          // Ring of Retribution: artifact, vitality +1
					// Endurance of Dwarves: a condition borne by a Dwarf, vitality +1.  Not an item.
					put("endurance", "2_4");
					put("endurance2", "2_4");
					// Items a Man can bear, for Aragorn.
					put("sword", "1_112");       // Ranger's Sword: possession
					put("gondorArmor", "1_92");  // Armor: possession
					// Spider Poison: a Shadow possession that its owner transfers to a companion.  "Each time bearer is
					// assigned to a skirmish, bearer must exert", and nobody is assigned here.
					put("poison", "8_28");
					// Black Breath: a Shadow condition, "Burdens and wounds may not be removed from bearer".  Its owner
					// transfers it to a character skirmishing a Nazgul; here it is cheated on.
					put("breath", "1_207");

					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("savage", "1_151");      // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 and no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OneDwarfInMoriaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: One Dwarf in Moria
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot a Dwarf bearing 2 or more items to heal that Dwarf.  Return X items on that Dwarf to
		 * their owners' hands to wound X minions.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("onedwarf");

		assertEquals("One Dwarf in Moria", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void HealsADwarfBearingTwoItemsOnceAndCannotBePlayedOnceHeBearsOnlyOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var onedwarf = scn.GetFreepsCard("onedwarf");
		var onedwarf2 = scn.GetFreepsCard("onedwarf2");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		var endurance = scn.GetFreepsCard("endurance");
		var sword = scn.GetFreepsCard("sword");
		var gondorArmor = scn.GetFreepsCard("gondorArmor");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli, aragorn);
		// Two items and a condition on Gimli; two items on Aragorn, who is no Dwarf.
		scn.AttachCardsTo(gimli, axe, armor, endurance);
		scn.AttachCardsTo(aragorn, sword, gondorArmor);
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveCardsToHand(onedwarf, onedwarf2);

		scn.StartGame();

		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);
		// An exact pool to count from.
		scn.SetTwilight(0);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsPlayAvailable(onedwarf));

		scn.FreepsPlayCard(onedwarf);

		// Gimli is the only Dwarf bearing two items, so he is spotted without a choice.  One wound is healed, not both.
		assertEquals(1, scn.GetWoundsOn(gimli));

		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, armor));
		scn.FreepsChooseCard(axe);
		assertInZone(Zone.HAND, axe);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(endurance, gimli);

		// One item returned, so one minion is wounded, and there are two to choose from.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(savage);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInDiscard(onedwarf);
		assertEquals(1, scn.GetFreepsDiscardCount());
		// The second copy and the Dwarven Axe.
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.ShadowPass();

		// Gimli is still wounded and still bears two cards, but only one of them is an item.  Aragorn's two items
		// do not make a Dwarf of him.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetAttachedCards(gimli).size());
		assertEquals(2, scn.GetAttachedCards(aragorn).size());
		assertInHand(onedwarf2);
		assertFalse(scn.FreepsPlayAvailable(onedwarf2));
	}

	@Test
	public void AnyDwarfBearingTwoItemsCanBeSpottedAndOnlyItemsOnThatDwarfCanBeReturned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var onedwarf = scn.GetFreepsCard("onedwarf");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axe = scn.GetFreepsCard("axe");
		var ring = scn.GetFreepsCard("ring");
		var armor = scn.GetFreepsCard("armor");
		var bracers = scn.GetFreepsCard("bracers");
		var endurance = scn.GetFreepsCard("endurance");
		var handaxe = scn.GetFreepsCard("handaxe");
		var endurance2 = scn.GetFreepsCard("endurance2");
		var sword = scn.GetFreepsCard("sword");
		var gondorArmor = scn.GetFreepsCard("gondorArmor");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli, guard, aragorn);
		// Grimir is an ally, so he sits in the support area.
		scn.MoveCardsToSupportArea(grimir);
		// A companion bearing a possession and an artifact.
		scn.AttachCardsTo(gimli, axe, ring);
		// An ally bearing two possessions and a condition.
		scn.AttachCardsTo(grimir, armor, bracers, endurance);
		// A Dwarf bearing two cards of which only one is an item.
		scn.AttachCardsTo(guard, handaxe, endurance2);
		// Two items, on a Man.
		scn.AttachCardsTo(aragorn, sword, gondorArmor);
		// Everybody has a wound to heal, so being wounded sets nobody apart.  (Dwarf Guard has vitality 2, +1
		// for Endurance of Dwarves.)
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(grimir, 1);
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveCardsToHand(onedwarf);

		scn.StartGame();

		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(onedwarf));
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.FreepsPlayCard(onedwarf);

		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, aragorn, frodo));
		scn.FreepsChooseCard(grimir);

		// Only the Dwarf who was spotted is healed.
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Grimir's two possessions, and neither his condition nor anything borne by someone else.
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, bracers));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(endurance, axe, ring, handaxe, endurance2, sword, gondorArmor));
		assertEquals(0, scn.GetFreepsHandCount());
		scn.FreepsChooseCard(bracers);

		assertInZone(Zone.HAND, bracers);
		assertEquals(1, scn.GetFreepsHandCount());
		assertAttachedTo(armor, grimir);
		assertAttachedTo(endurance, grimir);
		assertEquals(2, scn.GetAttachedCards(gimli).size());

		// Minions of either culture, and no one else.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, grimir, guard, aragorn, frodo));
		scn.FreepsChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInDiscard(onedwarf);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ReturnsEveryItemOnTheDwarfToItsOwnersHandAndWoundsThatManyMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var onedwarf = scn.GetFreepsCard("onedwarf");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var ring = scn.GetFreepsCard("ring");
		// The text says "their owners' hands", so one of the three items is the Shadow player's: a Shadow
		// possession of the kind that is transferred to a companion, put on Gimli by cheating.
		var poison = scn.GetShadowCard("poison");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe, ring, poison);
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveCardsToHand(onedwarf);

		scn.StartGame();

		scn.MoveMinionsToTable(troop, troop2, troop3, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetAttachedCards(gimli).size());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowHandCount());

		scn.FreepsPlayCard(onedwarf);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// A possession, an artifact and the Shadow player's possession: all three are items on Gimli.
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, ring, poison));
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(axe, ring, poison);

		assertEquals(0, scn.GetAttachedCards(gimli).size());
		assertInZone(Zone.HAND, axe, ring, poison);
		// Mine come back to my hand, and the opponent's goes to the opponent's.
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.GetShadowHand().contains(poison));

		// Three items returned, so exactly three of the four minions are wounded.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, troop2, troop3, savage));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(troop, savage, troop3);

		// One wound each.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(troop3));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertInDiscard(onedwarf);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnUnwoundedDwarfCanStillBeSpottedToReturnItemsAndWoundMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var onedwarf = scn.GetFreepsCard("onedwarf");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe, armor);
		scn.MoveCardsToHand(onedwarf);

		scn.StartGame();

		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// Spotting is a requirement and the heal an effect: with no wound to heal the event is still playable.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsPlayAvailable(onedwarf));

		scn.FreepsPlayCard(onedwarf);
		assertEquals(0, scn.GetWoundsOn(gimli));

		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, armor));
		scn.FreepsChooseCards(axe, armor);
		assertInZone(Zone.HAND, axe, armor);
		assertEquals(2, scn.GetFreepsHandCount());

		// Two items returned and two minions on the table: both are wounded without a choice.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(onedwarf);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ReturningMoreItemsThanThereAreMinionsWoundsEachMinionOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var onedwarf = scn.GetFreepsCard("onedwarf");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		var bracers = scn.GetFreepsCard("bracers");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe, armor, bracers);
		scn.AddWoundsToChar(gimli, 1);
		scn.MoveCardsToHand(onedwarf);

		scn.StartGame();

		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.FreepsPlayCard(onedwarf);
		assertEquals(0, scn.GetWoundsOn(gimli));

		// Returning items is the cost and is the player's to pay in full, whatever the effect can then reach.
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, armor, bracers));
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(axe, armor, bracers);
		assertInZone(Zone.HAND, axe, armor, bracers);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetAttachedCards(gimli).size());

		// Three minions should be wounded and there are two: as much as can be done is done, one wound each.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(onedwarf);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ReturningNoItemsStillHealsTheDwarfAndWoundsNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var onedwarf = scn.GetFreepsCard("onedwarf");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe, armor);
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveCardsToHand(onedwarf);

		scn.StartGame();

		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetFreepsHandCount());

		scn.FreepsPlayCard(onedwarf);
		assertEquals(1, scn.GetWoundsOn(gimli));

		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, armor));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertAttachedTo(axe, gimli);
		assertAttachedTo(armor, gimli);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInDiscard(onedwarf);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ReturningAnItemWhoseLossKillsTheDwarfWoundsOnlyAsManyMinionsAsItemsWereReturned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var onedwarf = scn.GetFreepsCard("onedwarf");
		var gimli = scn.GetFreepsCard("gimli");
		var ring = scn.GetFreepsCard("ring");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		// The Shadow player's Black Breath, so that the event's heal does not take a wound off Gimli.
		var breath = scn.GetShadowCard("breath");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, ring, axe, armor, breath);
		// Vitality 3 + 1 for Ring of Retribution, and 3 wounds: only the Ring is keeping him alive.
		scn.AddWoundsToChar(gimli, 3);
		scn.MoveCardsToHand(onedwarf);

		scn.StartGame();

		scn.MoveMinionsToTable(troop, troop2, troop3, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetWoundsOn(gimli));
		assertTrue(scn.IsExhausted(gimli));
		assertEquals(0, scn.GetFreepsDeadCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsPlayAvailable(onedwarf));

		scn.FreepsPlayCard(onedwarf);

		// Black Breath refuses the heal.  A card may be played even if it will have no effect.
		assertEquals(3, scn.GetWoundsOn(gimli));

		// The three items, and not the condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, armor, ring));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(breath));
		scn.FreepsChooseCard(ring);

		// The Ring goes back to hand and its vitality bonus with it, which leaves Gimli at vitality 3 with
		// 3 wounds: he is killed, and the two items he still bore are discarded, not returned.
		assertInZone(Zone.HAND, ring);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.DEAD, gimli);
		assertInDiscard(axe, armor);

		// One item was returned, so exactly one of the four minions is wounded.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, troop2, troop3, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(troop2);

		assertEquals(1, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(troop3));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInDiscard(onedwarf);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
