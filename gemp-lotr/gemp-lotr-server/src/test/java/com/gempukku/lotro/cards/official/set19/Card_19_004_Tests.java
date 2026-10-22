package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thatstwo", "19_4");
					put("gimli", "19_2");       // Gimli, Opinionated Guide: a fellowship companion, vitality 3
					put("swarms1", "1_183");    // Goblin Swarms: a Shadow condition in the support area; its texts need a [moria] Orc winning a skirmish or stacked on it
					put("swarms2", "1_183");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetBoardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thatstwo", "19_4");
					put("gimli", "19_2");       // Gimli, Opinionated Guide: [dwarven] Dwarf, a fellowship companion; draws on a [dwarven] possession played
					put("boromir", "19_13");    // Boromir, Destined Guide: [gondor] Man, a fellowship companion; his trigger needs a won skirmish
					put("oldgimli", "1_13");    // Gimli, Son of Gloin: a Gimli who is no fellowship companion; a skirmish ability never used
					put("farin", "1_11");       // Farin, Dwarven Emissary: a [dwarven] companion who is no Gimli; +2 against an Orc only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no fellowship companion; a maneuver ability never used
					put("armor", "1_8");        // Dwarven Armor: a Free Peoples possession on a Dwarf

					put("savage", "1_151");     // Uruk Savage: a minion; damage +1 is its whole text
					put("poison", "8_28");      // Spider Poison: a Shadow possession in the support area; its texts need Shelob or a bearer
					put("breath", "1_207");     // Black Breath: a Shadow condition in the support area; its texts need a Nazgul or a bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThatsTwoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: That's Two!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Spot Gimli and exert a fellowship companion to have each Shadow player discard one of his or her Shadow cards from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("thatstwo");

		assertEquals("That's Two!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AHinderedShadowCardMayBeTheOneTheShadowPlayerDiscards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("thatstwo");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(card);

		var hindered = scn.GetShadowCard("swarms1");
		var unhindered = scn.GetShadowCard("swarms2");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(hindered, unhindered);
		assertInZone(Zone.SUPPORT, hindered, unhindered);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// A hindered card has no type or culture, and is a Shadow card still (CR 5.0 "hinder"; Ketura on Q-0098:
		// "one of his or her Shadow cards" may be a hindered one).
		scn.HinderCard(hindered);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.IsHindered(hindered));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsPlayAvailable(card));

		// Gimli is the one fellowship companion, and exerts.
		scn.FreepsPlayCard(card);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// The Shadow player picks among his Shadow cards in play, the hindered condition among them.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hindered, unhindered, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(hindered);

		assertInZone(Zone.DISCARD, hindered);
		assertInZone(Zone.SUPPORT, unhindered);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ExertsAFellowshipCompanionToHaveTheShadowPlayerDiscardOneOfHisShadowCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBoardScenario();

		var thatsTwo = scn.GetFreepsCard("thatstwo");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var poison = scn.GetShadowCard("poison");
		var breath = scn.GetShadowCard("breath");
		scn.MoveCompanionsToTable(gimli, boromir, aragorn);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToHand(thatsTwo);
		scn.MoveCardsToSupportArea(poison, breath);
		assertAttachedTo(armor, gimli);
		assertInZone(Zone.SUPPORT, poison, breath);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsPlayAvailable(thatsTwo));

		scn.FreepsPlayCard(thatsTwo);
		// The cost: a companion with the Fellowship keyword, of either culture; Aragorn and Frodo have none.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		scn.FreepsChooseCard(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// The Shadow player chooses one of his Shadow cards in play: a minion, a possession, a condition.
		// The Free Peoples cards are not his to choose.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, poison, breath));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(armor, gimli, aragorn));
		// Those three and no more: his sites are his cards but are not Shadow cards, and are not offered.
		assertEquals(3, scn.ShadowGetCardChoices().size());
		scn.ShadowChooseCard(poison);

		assertInDiscard(poison, thatsTwo);
		assertInZone(Zone.SUPPORT, breath);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertAttachedTo(armor, gimli);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void IsNotPlayableWithoutGimli() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBoardScenario();

		var thatsTwo = scn.GetFreepsCard("thatstwo");
		var boromir = scn.GetFreepsCard("boromir");
		var farin = scn.GetFreepsCard("farin");
		var savage = scn.GetShadowCard("savage");
		// A fellowship companion who can exert, and a [dwarven] companion who is no Gimli.
		scn.MoveCompanionsToTable(boromir, farin);
		scn.MoveCardsToHand(thatsTwo);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(thatsTwo));
	}

	@Test
	public void AGimliWhoIsNoFellowshipCompanionIsSpottedAndDoesNotPay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBoardScenario();

		var thatsTwo = scn.GetFreepsCard("thatstwo");
		var oldGimli = scn.GetFreepsCard("oldgimli");
		var boromir = scn.GetFreepsCard("boromir");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(oldGimli, boromir);
		scn.MoveCardsToHand(thatsTwo);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(0, scn.GetWoundsOn(oldGimli));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsPlayAvailable(thatsTwo));

		scn.FreepsPlayCard(thatsTwo);
		// Boromir is the one fellowship companion: he exerts, and this Gimli does not.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(oldGimli));
		// The Savage is the Shadow player's one Shadow card in play.
		assertInDiscard(savage, thatsTwo);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void IsNotPlayableWhenNoFellowshipCompanionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBoardScenario();

		var thatsTwo = scn.GetFreepsCard("thatstwo");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(thatsTwo);
		// Gimli, vitality 3, exhausted; Aragorn could exert but is no fellowship companion.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(thatsTwo));
	}

	@Test
	public void WithNoShadowCardInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBoardScenario();

		var thatsTwo = scn.GetFreepsCard("thatstwo");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToHand(thatsTwo);
		assertAttachedTo(armor, gimli);

		scn.StartGame();
		// No minion: the turn goes from the move straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsPlayAvailable(thatsTwo));

		scn.FreepsPlayCard(thatsTwo);
		// Gimli pays, and the Shadow player has nothing to discard and is asked nothing.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInDiscard(thatsTwo);
		assertAttachedTo(armor, gimli);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
