package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "11_9");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3, damage +1; his own skirmish ability is used only on purpose
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion; strength +2 while skirmishing an Orc
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally; a maneuver ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path with Uruk Camp 4_335 as site 2: a battleground (and a plains).  Its trigger answers an Uruk-hai
	// played, and the minions here are cheated in.
	protected VirtualTableScenario GetUrukCampScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "11_9");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3, damage +1; his own skirmish ability is used only on purpose
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion; strength +2 while skirmishing an Orc
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai, strength 5, vitality 3; its texts are about unbound Hobbits
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1 (exhausted); its play trigger is not reached by a cheat
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path as it is: site 2, Eastfold 4_331, is a plains and no battleground (its Shadow ability needs 3
	// [isengard] trackers); site 4, White Rocks 4_346, is a battleground and nothing else (its Shadow number counts
	// mounted companions, and none is).
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "11_9");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3, damage +1; his own skirmish ability is used only on purpose
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai, strength 5, vitality 3; its texts are about unbound Hobbits
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1 (exhausted); its play trigger is not reached by a cheat
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimlisBattleAxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Gimli's Battle Axe, Vicious Weapon
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be a Dwarf.<br>Bearer gains <b>muster</b>. <helper>(At the start of the regroup phase, you may discard a card from hand to draw a card.)</helper><br><b>Skirmish:</b> If bearer is Gimli and he is at a battleground site, exert him to wound a minion he is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe");

		assertEquals("Gimli's Battle Axe", card.getBlueprint().getTitle());
		assertEquals("Vicious Weapon", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void CanBePlayedOnADwarfCompanionOrADwarfAllyWhoGainsMusterAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(axe);

		scn.StartGame();
		assertInHand(axe);
		assertFalse(scn.HasKeyword(thrarin, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(gimli, Keyword.MUSTER));

		assertTrue(scn.FreepsPlayAvailable(axe));
		scn.FreepsPlayCard(axe);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(thrarin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		// On the ally: he gains muster, and Gimli, a Dwarf who does not bear it, does not.
		scn.FreepsChooseCard(thrarin);
		assertAttachedTo(axe, thrarin);
		assertTrue(scn.HasKeyword(thrarin, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(gimli, Keyword.MUSTER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionBearerGainsMusterAndAnotherDwarfDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.AttachCardsTo(gimli, axe);

		scn.StartGame();
		assertAttachedTo(axe, gimli);
		assertTrue(scn.HasKeyword(gimli, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(farin, Keyword.MUSTER));
	}

	@Test
	public void AtABattlegroundGimliExertsToWoundAMinionHeIsSkirmishingAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.AttachCardsTo(gimli, axe);

		var pursuer = scn.GetShadowCard("pursuer");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(axe, gimli);
		scn.MoveMinionsToTable(pursuer, runner, troop);
		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, pursuer}, new PhysicalCardImpl[]{farin, troop});
		scn.ShadowAssignToMinions(gimli, runner);

		// Farin's skirmish first, Gimli assigned and waiting: the ability is offered and paid for, and no minion is
		// one he is skirmishing, so nothing is wounded.
		scn.FreepsResolveSkirmish(farin);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsActionAvailable(axe));
		scn.FreepsUseCardAction(axe);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Farin's 7 (5 + 2 against an Orc) loses to the Troop's 13 and takes a wound.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(farin));

		// Gimli's skirmish: the two minions he skirmishes are offered, the Troop, which skirmished Farin, is not.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.FreepsActionAvailable(axe));
		scn.FreepsUseCardAction(axe);
		assertTrue(scn.FreepsHasCardChoiceAvailable(pursuer));
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		// The Runner, of vitality 1, is wounded and so killed: a wound, not an exertion.
		scn.FreepsChooseCard(runner);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertInDiscard(runner);
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(troop));

		// Gimli is now exhausted (2 wounds on vitality 3): the ability is no longer offered, the Pursuer still there.
		scn.ShadowPass();
		assertInZone(Zone.SHADOW_CHARACTERS, pursuer);
		assertFalse(scn.FreepsActionAvailable(axe));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BorneByADwarfWhoIsNotGimliItIsNotOfferedThoughGimliIsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.AttachCardsTo(farin, axe);

		var pursuer = scn.GetShadowCard("pursuer");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(axe, farin);
		scn.MoveMinionsToTable(pursuer, troop);
		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{farin, pursuer}, new PhysicalCardImpl[]{gimli, troop});

		scn.FreepsResolveSkirmish(farin);
		assertEquals(0, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsActionAvailable(axe));
		// Gimli's own skirmish ability is still on offer: the player is being asked.
		assertTrue(scn.FreepsActionAvailable(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtASiteThatIsNoBattlegroundItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		assertAttachedTo(axe, gimli);
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.FreepsAssignToMinions(gimli, pursuer);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsActionAvailable(axe));
		assertTrue(scn.FreepsActionAvailable(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtABattlegroundOfNoOtherTerrainItIsOfferedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.SkipToSite(3);
		// Laid after the skip, whose turns would otherwise stop on the bearer's muster offer.
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);
		assertAttachedTo(axe, gimli);
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		scn.FreepsAssignToMinions(gimli, pursuer);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsActionAvailable(axe));
		scn.FreepsUseCardAction(axe);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
