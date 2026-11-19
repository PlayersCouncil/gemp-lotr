package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_047_Tests
{

	// The Hobbit table, with Town Hall as site 6 in place of Esgaroth 30_54.  The cards are laid once the
	// fellowship is at Town Hall, in its fellowship phase.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bard", "30_22");       // Bard, The Bowman: [esgaroth] Man ally, vitality 3; a Regroup ability, not used
					put("percy", "32_19");      // Percy, Bard's Lieutenant: [esgaroth] Man ally; a wound-prevention Response, not used
					put("elrond", "30_21");     // Elrond, Elven Lord: [elven] Elf ally; a start-of-turn heal and a Regroup ability, not used
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion; his ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion; his ability not used
					put("axe", "31_1");         // Dwarven Axe: [dwarven] possession, hand weapon; bearer a [dwarven] character
					put("orcrist", "30_17");    // Orcrist: [dwarven] artifact, hand weapon; bearer Thorin
					put("arrow", "32_16");      // Black Arrow: [esgaroth] possession, ranged weapon; bearer Bard
					put("armor", "33_3");       // Balin's Armor: [dwarven] possession, armor (no weapon); bearer Balin
					put("forged", "32_18");     // Iron-Forged Weapon: [esgaroth] hand weapon, laid in the Shadow player's discard pile
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.HobbitSites);
					put("site6", "32_47");
				}},
				null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TownHallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Town Hall
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 6
		 * Shadow Number: 3
		 * Direction: none
		 * Game Text: <b>River</b>. <b>Sanctuary</b>. <b>Fellowship:</b> Exert a [esgaroth] ally to play a weapon from your discard pile (limit once per turn).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(6);

		assertEquals("Town Hall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(6, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void FellowshipAbilityExertsAnEsgarothAllyToPlayAWeaponFromTheDiscardPileOnceATurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bard = scn.GetFreepsCard("bard");
		var percy = scn.GetFreepsCard("percy");
		var elrond = scn.GetFreepsCard("elrond");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var orcrist = scn.GetFreepsCard("orcrist");
		var arrow = scn.GetFreepsCard("arrow");
		var armor = scn.GetFreepsCard("armor");
		var forged = scn.GetShadowCard("forged");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(thorin, balin);
		scn.MoveAlliesToTable(bard, percy, elrond);
		scn.MoveCardsToDiscard(axe, orcrist, arrow, armor, forged);
		scn.FreepsRefreshDecision();
		var townHall = scn.GetCurrentSite();
		assertEquals("32_47", townHall.getBlueprintId());
		assertInDiscard(axe, orcrist, arrow, armor, forged);
		assertEquals(0, scn.GetWoundsOn(bard));
		assertEquals(0, scn.GetWoundsOn(percy));
		assertEquals(0, scn.GetWoundsOn(elrond));

		assertTrue(scn.FreepsActionAvailable(townHall));
		scn.FreepsUseCardAction(townHall);

		// [esgaroth] allies of two titles; not an ally of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bard, percy));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(percy);
		assertEquals(1, scn.GetWoundsOn(percy));
		assertEquals(0, scn.GetWoundsOn(bard));
		assertEquals(0, scn.GetWoundsOn(elrond));

		// A hand weapon possession, a hand weapon artifact and a ranged weapon; not armor, nor a weapon in the
		// Shadow player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, orcrist, arrow));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armor, forged));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(orcrist);

		// Thorin is Orcrist's only bearer.
		assertAttachedTo(orcrist, thorin);
		assertInDiscard(axe, arrow, armor, forged);

		// Once a turn: not offered again, though Bard can still exert and two weapons are left.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(townHall));
	}

	@Test
	public void IsNotOfferedWhileTheOnlyEsgarothAllyIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bard = scn.GetFreepsCard("bard");
		var elrond = scn.GetFreepsCard("elrond");
		var thorin = scn.GetFreepsCard("thorin");
		var axe = scn.GetFreepsCard("axe");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(bard, elrond);
		// Vitality 3 with 2 wounds: Bard cannot exert.  Elrond, an ally of another culture, can.
		scn.AddWoundsToChar(bard, 2);
		scn.MoveCardsToDiscard(axe);
		scn.FreepsRefreshDecision();
		var townHall = scn.GetCurrentSite();
		assertEquals("32_47", townHall.getBlueprintId());
		assertEquals(2, scn.GetWoundsOn(bard));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertInDiscard(axe);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(townHall));
	}

	@Test
	public void IsNotOfferedWithNoWeaponInTheDiscardPileThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bard = scn.GetFreepsCard("bard");
		var percy = scn.GetFreepsCard("percy");
		var balin = scn.GetFreepsCard("balin");
		var arrow = scn.GetFreepsCard("arrow");
		var armor = scn.GetFreepsCard("armor");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(percy);
		// The Black Arrow, with no Bard to bear it, and Balin's Armor, which Balin could bear but is no weapon.
		scn.MoveCardsToDiscard(arrow, armor);
		scn.FreepsRefreshDecision();
		var townHall = scn.GetCurrentSite();
		assertEquals("32_47", townHall.getBlueprintId());
		assertInDiscard(arrow, armor);
		assertEquals(0, scn.GetWoundsOn(percy));

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(townHall));

		// Bard in play: the Arrow can be played.
		scn.MoveAlliesToTable(bard);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(townHall));
	}
}
