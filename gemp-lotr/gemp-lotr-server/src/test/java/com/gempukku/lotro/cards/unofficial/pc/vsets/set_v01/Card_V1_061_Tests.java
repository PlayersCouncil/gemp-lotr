package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_061_Tests
{
	// The Ring-bearer is Frodo, Son of Drogo: a [shire] Hobbit, Ring-bound, vitality 4.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Aragorn, Ranger of the North: [gondor] Man, vitality 4.
					// "Maneuver: Exert Aragorn to make him defender +1 until the regroup phase."
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: [dwarven] Dwarf, strength 6, vitality 3, damage +1.
					// "Skirmish: Exert Gimli to make him strength +2."
					put("gimli", "1_13");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] companion, vitality 3

					// Celeborn, Lord of Lorien: [elven] ally, home site 6, vitality 3; a fellowship ability only
					put("celeborn", "1_34");
					// Saelbeth, Elven Councilor: [elven] ally, home site 3, vitality 3.
					// "Skirmish: Exert Saelbeth to make an Uruk-hai strength -1."
					put("saelbeth", "3_25");
					// Uruviel, Maid of Lorien: [elven] ally, home site 6, vitality 2.
					// "While you can spot your site 6, Uruviel has the game text of that site."
					put("uruviel", "1_67");
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] Dwarf ally, vitality 3
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] Hobbit ally, vitality 3

					// Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 and nothing else
					put("savage", "1_151");
					put("savage2", "1_151");
					// One of You Must Do This: "Maneuver: Exert an [isengard] minion to make the Free Peoples player
					// exert X companions, where X is the number of these races you can spot in the fellowship: Dwarf,
					// Elf, Man, and Wizard."  Costs 1.
					put("oneOfYou", "3_63");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "101_61");
					// The Great River and not Anduin Confluence, which discards every ally
					put("site7", "3_118");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LorienThroneRoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Lorien Throne Room
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 6
		 * Shadow Number: 3
		 * Direction: Left
		 * Game Text: Forest. Sanctuary. Each time a companion exerts here, you may exert an [elven] ally to heal that
		 * 	companion (limit once per phase).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(6);

		assertEquals("Lorien Throne Room", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(6, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void WhenACompanionExertsHereAnElvenAllyMayExertToHealThatCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var saelbeth = scn.GetFreepsCard("saelbeth");
		var grimir = scn.GetFreepsCard("grimir");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCompanionsToTable(aragorn, gimli, arwen);
		scn.MoveAlliesToTable(celeborn, saelbeth, grimir, bilbo);

		// No ally is an Elf of another culture, or [elven] and of another race: Grimir is made an Elf and stays
		// [dwarven]; Bilbo is made [elven] and stays a Hobbit.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, grimir, null, Race.ELF));
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, bilbo, null, Culture.ELVEN));

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(5);

		scn.MoveMinionsToTable(savage);
		// Gimli is wounded and stands by.
		scn.AddWoundsToChar(gimli, 1);
		scn.FreepsPass();

		var throneRoom = scn.GetCurrentSite();
		assertEquals("Lorien Throne Room", throneRoom.getBlueprint().getTitle());

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(throneRoom));
		scn.FreepsAcceptOptionalTrigger();

		// Who may exert for it: the [elven] allies, of either home site, and Bilbo.  Not Arwen, an [elven] companion;
		// not Grimir, an ally who is an Elf and not [elven].
		assertTrue(scn.FreepsHasCardChoiceAvailable(celeborn, saelbeth, bilbo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, grimir, aragorn, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(saelbeth));
		scn.FreepsChooseCard(saelbeth);

		assertEquals(1, scn.GetWoundsOn(saelbeth));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(grimir));
		// The companion who exerted is the one healed; nobody is asked which, and Gimli keeps his wound.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DecliningExertsAndHealsNobodyAndDoesNotUseUpThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(celeborn);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(5);

		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();

		var throneRoom = scn.GetCurrentSite();
		assertEquals("Lorien Throne Room", throneRoom.getBlueprint().getTitle());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(throneRoom));

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Declining is not using it: Aragorn's next exertion in the same maneuver phase is offered it again.
		scn.ShadowPass();
		scn.FreepsUseCardAction(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(throneRoom));

		// Celeborn is the only [elven] ally, so he is not asked for.
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void HealsOnceInASkirmishAndAgainInTheNextSkirmishOfTheSameTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(celeborn);

		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.SkipToSite(5);

		scn.MoveMinionsToTable(savage, savage2);
		scn.FreepsPass();

		var throneRoom = scn.GetCurrentSite();
		assertEquals("Lorien Throne Room", throneRoom.getBlueprint().getTitle());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, savage},
				new PhysicalCardImpl[]{aragorn, savage2}
		);

		// Gimli's skirmish.  He exerts, and Celeborn (vitality 3) exerts to heal him.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(throneRoom));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(celeborn));

		// He exerts again in the same skirmish: nothing is offered, though Celeborn could exert again.
		scn.ShadowPass();
		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.IsExhausted(celeborn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 6 + 2 + 2 against 5: the Savage is overwhelmed.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Aragorn's skirmish is another phase.  Gimli exerts in it, and is offered the heal again.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishing(aragorn));
		scn.FreepsUseCardAction(gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(throneRoom));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(celeborn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAllyExertingHereIsOfferedNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var celeborn = scn.GetFreepsCard("celeborn");
		var saelbeth = scn.GetFreepsCard("saelbeth");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(celeborn, saelbeth);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(5);

		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();

		var throneRoom = scn.GetCurrentSite();
		assertEquals("Lorien Throne Room", throneRoom.getBlueprint().getTitle());

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, savage);

		// Saelbeth exerts for his own ability.  The Savage is the only Uruk-hai: 5 - 1.
		scn.FreepsUseCardAction(saelbeth);
		assertEquals(1, scn.GetWoundsOn(saelbeth));
		assertEquals(4, scn.GetStrength(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// On the same board, in the same skirmish, a companion's exertion is offered it.
		scn.ShadowPass();
		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(throneRoom));
	}

	@Test
	public void IsNotOfferedWhileNoElvenAllyCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.MoveAlliesToTable(celeborn, grimir);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(5);

		scn.MoveMinionsToTable(savage);
		// Celeborn (vitality 3) is exhausted.  Arwen, an [elven] companion, and Grimir, a [dwarven] ally, are not.
		scn.AddWoundsToChar(celeborn, 2);
		scn.FreepsPass();

		var throneRoom = scn.GetCurrentSite();
		assertEquals("Lorien Throne Room", throneRoom.getBlueprint().getTitle());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// With one wound taken off Celeborn, the next exertion is offered it.
		scn.RemoveWoundsFromChar(celeborn, 1);
		scn.ShadowPass();
		scn.FreepsUseCardAction(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(throneRoom));
	}

	@Test
	public void CompanionsMadeToExertTogetherAreEachOfferedByNameAndOnlyTheOneChosenIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(celeborn);

		var savage = scn.GetShadowCard("savage");
		var oneOfYou = scn.GetShadowCard("oneOfYou");

		scn.StartGame();
		scn.SkipToSite(5);

		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToHand(oneOfYou);
		scn.FreepsPass();

		var throneRoom = scn.GetCurrentSite();
		assertEquals("Lorien Throne Room", throneRoom.getBlueprint().getTitle());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// A Man and a Dwarf in the fellowship: the Free Peoples player exerts 2 companions, and chooses the
		// Ring-bearer and Gimli.
		scn.ShadowPlayCard(oneOfYou);
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.FreepsChooseCards(frodo, gimli);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// One offer for each, and each says whom it would heal.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(throneRoom));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("Frodo, Son of Drogo"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("Gimli, Son of Gl"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable("Aragorn"));
		scn.FreepsChooseAction("Frodo, Son of Drogo");

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(celeborn));
		// Once in a phase: the offer for Gimli is gone with it.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ACompanionExertingAtAnotherSiteIsOfferedNothingEvenWithUruvielBorrowingTheText() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var uruviel = scn.GetFreepsCard("uruviel");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(uruviel);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(5);

		// The Free Peoples player's own copy is laid as site 6, so that it is "your site 6" to Uruviel.
		var throneRoom = scn.GetFreepsSite(6);
		scn.MoveCardToAdventurePath(throneRoom);

		scn.SkipToSite(6);
		assertEquals(throneRoom, scn.GetCurrentSite());
		assertEquals(scn.P1(), throneRoom.getOwner());

		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(uruviel));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithUruvielBorrowingTheTextACompanionExertingHereIsHealedOnceByTheSiteAndOnceByUruvielInAPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		var uruviel = scn.GetFreepsCard("uruviel");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(celeborn, uruviel);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(5);

		// The Free Peoples player's own copy is laid as site 6, so that it is "your site 6" to Uruviel.
		var throneRoom = scn.GetFreepsSite(6);
		scn.MoveCardToAdventurePath(throneRoom);

		scn.MoveMinionsToTable(savage);
		// Aragorn (vitality 4) starts with a wound, so that a second heal has something to take off.
		scn.AddWoundsToChar(aragorn, 1);
		scn.FreepsPass();
		assertEquals(throneRoom, scn.GetCurrentSite());
		assertEquals(scn.P1(), throneRoom.getOwner());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// "Here" is Lorien Throne Room, and that is where the fellowship is: the site offers, and so does Uruviel,
		// who has its text.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(throneRoom));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(uruviel));

		scn.FreepsChooseOptionalTrigger(throneRoom);
		assertTrue(scn.FreepsHasCardChoiceAvailable(celeborn, uruviel));
		scn.FreepsChooseCard(celeborn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(uruviel));

		// The site has healed once this phase.  Uruviel's offer for the same exertion is still to be answered.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(throneRoom));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(uruviel));

		// Uruviel is an [elven] ally herself, and may be the one who exerts.
		scn.FreepsChooseOptionalTrigger(uruviel);
		assertTrue(scn.FreepsHasCardChoiceAvailable(celeborn, uruviel));
		scn.FreepsChooseCard(uruviel);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(uruviel));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Once in the phase for the site and once for Uruviel, and no more: Aragorn's next exertion is offered
		// nothing, though Celeborn (vitality 3) could exert again.
		scn.ShadowPass();
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.IsExhausted(celeborn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
